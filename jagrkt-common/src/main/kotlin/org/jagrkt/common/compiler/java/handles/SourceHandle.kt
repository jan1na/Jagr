package org.jagrkt.common.compiler.java.handles

abstract class SourceHandle(val position: Int) {
  abstract fun process(sb: StringBuilder)
}
