package com.carrotsearch.randomizedtesting;

import java.io.Closeable;

/**
 * Allocation information (Thread, allocation stack) for tracking disposable
 * resources.
 */
final class CloseableResourceInfo {
  private final Closeable resource;
  private final LifecycleScope scope;
  private final Thread thread;
  private final StackTraceElement[] allocationStack;
  
  public CloseableResourceInfo(Closeable resource, LifecycleScope scope, Thread t, StackTraceElement[] allocationStack) {
    this.resource = resource;
    this.thread = t;
    this.allocationStack = allocationStack;
    this.scope = scope;
  }

  public Closeable getResource() {
    return resource;
  }

  public Thread getThread() {
    return thread;
  }
  
  public StackTraceElement[] getAllocationStack() {
    return allocationStack;
  }
  
  public LifecycleScope getScope() {
    return scope;
  }
}
