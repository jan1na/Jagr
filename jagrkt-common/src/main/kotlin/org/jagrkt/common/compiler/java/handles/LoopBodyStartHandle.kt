package org.jagrkt.common.compiler.java.handles

class LoopBodyStartHandle(position: Int) : SourceHandle(position) {
  override fun process(sb: StringBuilder) {
    sb.insert(position, "{ jagrinternal.instrumentation.TimeoutHandler.checkTimeout(); ")
  }
}
