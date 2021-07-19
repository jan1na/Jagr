/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common.compiler.java

import com.google.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jagrkt.api.testing.CompileResult
import org.jagrkt.common.compiler.java.handles.SourceHandle
import org.jagrkt.common.compiler.readEncoded
import org.jagrkt.common.testing.SubmissionInfoImpl
import org.jagrkt.common.testing.TestMeta
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.slf4j.Logger
import spoon.Launcher
import spoon.support.compiler.VirtualFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.jar.JarFile
import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import kotlin.collections.ArrayList

class RuntimeJarLoader @Inject constructor(
  private val logger: Logger,
) {

  fun loadCompiledJar(file: File): Map<String, CompiledClass> {
    val jarFile = JarFile(file)
    val classStorage: MutableMap<String, CompiledClass> = mutableMapOf()
    for (entry in jarFile.entries()) {
      when {
        entry.isDirectory -> continue
        entry.name.endsWith(".class") -> {
          val className = entry.name.replace('/', '.').substring(0, entry.name.length - 6)
          classStorage[className] =
            CompiledClass.Existing(className, jarFile.getInputStream(entry).use { it.readBytes() })
        }
        entry.name.endsWith("MANIFEST.MF") -> { // ignore
        }
        else -> logger.warn("$file jar entry $entry is not a java class file!")
      }
    }
    return classStorage
  }

  fun loadSourcesJar(file: File, runtimeClassPath: Map<String, CompiledClass> = mapOf()): CompileJarResult {
    val jarFile = try {
      JarFile(file)
    } catch (e: Exception) {
      return CompileJarResult(file)
    }
    if (file.nameWithoutExtension == "." || file.nameWithoutExtension == "..") {
      // Never let the students escape
      return CompileJarResult(file)
    }
    val sourceFiles: MutableMap<String, JavaSourceFile> = mutableMapOf()
    var submissionInfo: SubmissionInfoImpl? = null
    var testMeta: TestMeta? = null
    val instrumentationDir = Path.of("instrumentation", file.nameWithoutExtension)
    for (entry in jarFile.entries()) {
      when {
        entry.isDirectory -> continue
        entry.name == "meta-meta.json" -> {
          testMeta = try {
            Json { ignoreUnknownKeys = true }.decodeFromString<TestMeta>(
              jarFile.getInputStream(entry).bufferedReader().use { it.readText() })
          } catch (e: Exception) {
            logger.error("$file has invalid meta-meta.json", e)
            return CompileJarResult(file)
          }
          testMeta?.run {
            testMeta = copy(testClasses = testClasses.map { it.replace('\\', '/') })
          }
        }
        entry.name == "submission-info.json" -> {
          submissionInfo = try {
            Json { ignoreUnknownKeys = true }.decodeFromString<SubmissionInfoImpl>(
              jarFile.getInputStream(entry).bufferedReader().use { it.readText() })
          } catch (e: Throwable) {
            logger.error("$file has invalid submission-info.json", e)
            return CompileJarResult(file)
          }
        }
        entry.name.endsWith(".java") -> {
          val className = entry.name.replace('/', '.').substring(0, entry.name.length - 5)
          val content = jarFile.getInputStream(entry).use { it.readEncoded() }

          // Instrumentation
          val sourceHandles = ArrayList<SourceHandle>()
          Launcher().let {
            it.environment.complianceLevel = 15
            it.addInputResource(VirtualFile(content))
            it.addProcessor(LoopProcessor(sourceHandles))
            it.addProcessor(MethodProcessor(sourceHandles))
            it.buildModel()
            it.process()
          }
          sourceHandles.sortByDescending { it.position }
          val sb = StringBuilder(content)
          for (sourceHandle in sourceHandles) {
            sourceHandle.process(sb)
          }
          val transformedContent = sb.toString()
          val sourceFile = JavaSourceFile(className, entry.name, content, transformedContent)
          val instrumentedCodeFile = instrumentationDir.resolve(entry.name)
          instrumentedCodeFile.parent?.let { Files.createDirectories(it) }
          Files.writeString(instrumentedCodeFile, transformedContent)
          sourceFiles[entry.name] = sourceFile
        }
        entry.name.endsWith("MANIFEST.MF") -> { // ignore
        }
        else -> logger.warn("$file jar entry $entry is not a java source file!")
      }
    }
    if (sourceFiles.isEmpty()) {
      // no source files, skip compilation task
      return CompileJarResult(file, submissionInfo)
    }
    testMeta?.testClasses?.forEach { testClass ->
      if (sourceFiles.remove(testClass) != null) {
        logger.warn("$submissionInfo :: Removed test class $testClass")
      }
    }
    val compiledClasses: MutableMap<String, CompiledClass> = mutableMapOf()
    val collector = DiagnosticCollector<JavaFileObject>()
    val compiler = ToolProvider.getSystemJavaCompiler()
    val fileManager = ExtendedStandardJavaFileManager(
      compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8),
      runtimeClassPath,
      compiledClasses,
    )
    val result = compiler.getTask(null, fileManager, collector, null, null, sourceFiles.values).call()
    compiledClasses.linkSource(sourceFiles)
    if (!result || collector.diagnostics.isNotEmpty()) {
      val messages = mutableListOf<String>()
      var warnings = 0
      var errors = 0
      var other = 0
      for (diag in collector.diagnostics) {
        when (diag.kind) {
          Diagnostic.Kind.NOTE,
          Diagnostic.Kind.MANDATORY_WARNING,
          Diagnostic.Kind.WARNING,
          -> ++warnings
          Diagnostic.Kind.ERROR,
          -> ++errors
          else -> ++other
        }
        messages += "${diag.source.name}:${diag.lineNumber} ${diag.kind} :: ${diag.getMessage(Locale.getDefault())}"
      }
      return CompileJarResult(file, submissionInfo, compiledClasses, sourceFiles, messages, warnings, errors, other)
    }
    return CompileJarResult(file, submissionInfo, compiledClasses, sourceFiles)
  }

  private fun Map<String, CompiledClass>.linkSource(sourceFiles: Map<String, JavaSourceFile>) {
    for ((_, compiledClass) in this) {
      with(compiledClass.reader) {
        val packageName = with(className) {
          lastIndexOf('/').let {
            if (it == -1) null else substring(0, it)
          }
        }
        accept(object : ClassVisitor(Opcodes.ASM9) {
          override fun visitSource(source: String?, debug: String?) {
            if (source == null) return
            compiledClass.source = if (packageName == null) {
              sourceFiles["$source"]
            } else {
              sourceFiles["$packageName/$source"]
            }
          }
        }, ClassReader.SKIP_CODE)
      }
    }
  }

  data class CompileJarResult(
    val file: File,
    val submissionInfo: SubmissionInfoImpl? = null,
    val compiledClasses: Map<String, CompiledClass> = mapOf(),
    val sourceFiles: Map<String, JavaSourceFile> = mapOf(),
    private val messages: List<String> = listOf(),
    val warnings: Int = 0,
    val errors: Int = 0,
    val other: Int = 0,
  ) : CompileResult {
    override fun getMessages(): List<String> = messages
    override fun getWarningCount(): Int = warnings
    override fun getErrorCount(): Int = errors
    override fun getOtherCount(): Int = other

    fun printMessages(logger: Logger, lazyError: () -> String, lazyWarning: () -> String) {
      when {
        errors > 0 -> {
          logger.error(lazyError())
          for (message in messages) {
            logger.error(message)
          }
        }
        warnings > 0 -> {
          logger.warn(lazyWarning())
          for (message in messages) {
            logger.warn(message)
          }
        }
      }
    }

    fun copyWith(
      file: File? = null,
      submissionInfo: SubmissionInfoImpl? = null,
      compiledClasses: Map<String, CompiledClass>? = null,
      sourceFiles: Map<String, JavaSourceFile>? = null,
      messages: List<String>? = null,
      warnings: Int? = null,
      errors: Int? = null,
      other: Int? = null,
    ) = CompileJarResult(
      file ?: this.file,
      submissionInfo ?: this.submissionInfo,
      compiledClasses ?: this.compiledClasses,
      sourceFiles ?: this.sourceFiles,
      messages ?: this.messages,
      warnings ?: this.warnings,
      errors ?: this.errors,
      other ?: this.other,
    )
  }
}
