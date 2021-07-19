package org.jagrkt.common.compiler.java.handles

import org.jagrkt.common.Config

class LoopBodyEndHandle(override val position: Int, override val config: Config) : SourceHandle {
  override fun process(sb: StringBuilder) {
    if (config.instrumentations.timeoutSourcecode.enabled) {
      sb.insert(position, "}")
    }
  }
}
