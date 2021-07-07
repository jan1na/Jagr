package org.jagrkt.common.executor;

import org.jagrkt.api.executor.ExecutionContext;
import org.jagrkt.api.executor.ExecutionContextVerifier;

public class NoRecursiveAPIWrapper {
  public static void banRecursion(Runnable r) {
    ExecutionContext.runWithVerifiers(r, ExecutionContextVerifier.ensureNotRecursive());
  }
}
