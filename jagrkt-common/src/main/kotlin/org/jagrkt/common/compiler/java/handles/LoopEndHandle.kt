package org.jagrkt.common.compiler.java.handles

import org.jagrkt.common.Config

class LoopEndHandle(override val position: Int, override val config: Config) : SourceHandle {
  override fun process(sb: StringBuilder) {
    if (config.instrumentations.notIterativeSourcecode.enabled) {
      sb.insert(position, "}")
    }
  }
}
