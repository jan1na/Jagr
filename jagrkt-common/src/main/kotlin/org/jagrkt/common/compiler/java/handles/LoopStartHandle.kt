package org.jagrkt.common.compiler.java.handles

class LoopStartHandle(position: Int) : SourceHandle(position) {
  override fun process(sb: StringBuilder) {
    sb.insert(position, "{ jagrinternal.instrumentation.LoopHandler.willEnterLoop(); ")
  }
}
