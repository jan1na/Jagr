package org.jagrkt.common.compiler.java.handles

class LoopEndHandle(position: Int) : SourceHandle(position) {
  override fun process(sb: StringBuilder) {
    sb.insert(position, "}")
  }
}
