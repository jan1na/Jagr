package org.jagrkt.common.compiler.java

import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtCodeSnippetStatement
import spoon.reflect.code.CtLoop
import java.util.concurrent.atomic.AtomicInteger

class LoopProcessor(private val counter: AtomicInteger) : AbstractProcessor<CtLoop>() {

  override fun process(element: CtLoop?) {
    val codeTemplate = "org.jagrkt.common.testing.ExecutionContextStackResolver.INSTANCE.getValue().pushElement(%d)"
    val stmt = factory.createCodeSnippetStatement(String.format(codeTemplate, counter.incrementAndGet()))
    element!!.insertBefore<CtCodeSnippetStatement>(stmt)
  }
}
