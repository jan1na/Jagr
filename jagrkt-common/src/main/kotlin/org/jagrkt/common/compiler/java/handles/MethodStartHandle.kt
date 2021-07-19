package org.jagrkt.common.compiler.java.handles

import org.jagrkt.common.Config
import org.jagrkt.common.compiler.java.handles.SourceHandleStatements.notRecStmt
import org.jagrkt.common.compiler.java.handles.SourceHandleStatements.timeoutStmt

class MethodStartHandle(override val position: Int, override val config: Config) : SourceHandle {
  override fun process(sb: StringBuilder) {
    if (config.instrumentations.timeoutSourcecode.enabled && config.instrumentations.notRecursiveSourcecode.enabled)
      sb.insert(
        position,
        "{ $timeoutStmt $notRecStmt "
      )
    else if (config.instrumentations.timeoutSourcecode.enabled) {
      sb.insert(
        position,
        "{ $timeoutStmt "
      )
    } else if (config.instrumentations.notRecursiveSourcecode.enabled) {
      sb.insert(
        position,
        "{ $notRecStmt "
      )
    }
  }
}
