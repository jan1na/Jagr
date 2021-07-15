package org.jagrkt.common.compiler.java

import org.jagrkt.common.compiler.java.handles.LoopEndHandle
import org.jagrkt.common.compiler.java.handles.LoopStartHandle
import org.jagrkt.common.compiler.java.handles.SourceHandle
import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtLoop

class LoopProcessor(private val handles: MutableCollection<SourceHandle>) : AbstractProcessor<CtLoop>() {

  override fun process(element: CtLoop?) {
    element!!.position.let {
      handles += LoopStartHandle(it.sourceStart)
      handles += LoopEndHandle(it.sourceEnd + 1)
    }
  }
}
