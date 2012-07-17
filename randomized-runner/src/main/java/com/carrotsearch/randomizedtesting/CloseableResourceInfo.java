package com.carrotsearch.randomizedtesting;

import java.io.Closeable;

/**
 * Allocation information (Thread, allocation stack) for tracking disposable
 * resources.
 */
final class CloseableResourceInfo {
  private final Closeable resource;
  private final LifecycleScope scope;
  private final StackTraceElement[] allocationStack;
  private final String threadName;
  
  public CloseableResourceInfo(Closeable resource, LifecycleScope scope, Thread t, StackTraceElement[] allocationStack) {
    this.resource = resource;
    this.threadName = Threads.threadName(t);
    this.allocationStack = allocationStack;
    this.scope = scope;
  }

  public Closeable getResource() {
    return resource;
  }

  public StackTraceElement[] getAllocationStack() {
    return allocationStack;
  }
  
  public LifecycleScope getScope() {
    return scope;
  }

  /**
   * Return the allocating thread's name at the time of creating this resource info.
   */
  public String getThreadName() {
    return threadName;
  }
}
