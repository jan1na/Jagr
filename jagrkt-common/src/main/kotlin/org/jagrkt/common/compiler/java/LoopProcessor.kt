package org.jagrkt.common.compiler.java

import org.jagrkt.common.compiler.java.handles.*
import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtLoop

class LoopProcessor(private val handles: MutableCollection<SourceHandle>) : AbstractProcessor<CtLoop>() {

  override fun process(element: CtLoop) {
    element.position.let {
      handles += LoopStartHandle(it.sourceStart)
      handles += LoopEndHandle(it.sourceEnd + 1)
    }
    if (element.body == null) {
      handles += LoopBodyEndHandle(element.position.sourceEnd)
      handles += LoopBodyStartHandle(element.position.sourceEnd)
    } else {
      element.body.position.let {
        handles += LoopBodyStartHandle(it.sourceStart)
        handles += LoopBodyEndHandle(it.sourceEnd + 1)
      }
    }
  }
}
