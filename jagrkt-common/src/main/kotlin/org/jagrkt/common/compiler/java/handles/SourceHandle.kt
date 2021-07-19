package org.jagrkt.common.compiler.java.handles

import org.jagrkt.common.Config

interface SourceHandle {
  val position: Int
  val config: Config

  fun process(sb: StringBuilder)
}

object SourceHandleStatements {
  const val timeoutStmt = "jagrinternal.instrumentation.TimeoutHandler.checkTimeout();"
  const val notRecStmt = "jagrinternal.instrumentation.ExecutionContextHandler.checkExecutionContext();"
  const val notIterativeStmt = "jagrinternal.instrumentation.LoopHandler.willEnterLoop();"
}
