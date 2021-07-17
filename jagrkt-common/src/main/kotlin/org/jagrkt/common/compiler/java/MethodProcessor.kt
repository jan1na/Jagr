package org.jagrkt.common.compiler.java

import org.jagrkt.common.compiler.java.handles.*
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtMethod

class MethodProcessor(private val handles: MutableCollection<SourceHandle>) : AbstractProcessor<CtMethod<*>>() {

  override fun process(element: CtMethod<*>) {
    element.body?.position?.let {
      handles += MethodEndHandle(it.sourceEnd +1)
      handles += MethodStartHandle(it.sourceStart)
    }
  }
}
