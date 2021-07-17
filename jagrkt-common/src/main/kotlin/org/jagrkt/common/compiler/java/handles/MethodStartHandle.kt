package org.jagrkt.common.compiler.java.handles

class MethodStartHandle(position: Int) : SourceHandle(position) {
  override fun process(sb: StringBuilder) {
    sb.insert(position, "{ jagrinternal.instrumentation.TimeoutHandler.checkTimeout(); jagrinternal.instrumentation.ExecutionContextHandler.checkExecutionContext(); ")
  }
}
