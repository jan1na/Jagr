package org.jagrkt.common.compiler.java.handles

class MethodEndHandle(position: Int) : SourceHandle(position) {
  override fun process(sb: StringBuilder) {
    sb.insert(position, "}")
  }
}
