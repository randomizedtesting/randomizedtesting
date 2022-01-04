package com.carrotsearch.randomizedtesting.internal;

public final class Threads {
  Threads() {}

  /** Collect thread information, JVM vendor insensitive. */
  public static String threadName(Thread t) {
    return "Thread["
        + ("id=" + t.getId())
        + (", name=" + t.getName())
        + (", state=" + t.getState())
        + (", group=" + groupName(t.getThreadGroup()))
        + "]";
  }

  private static String groupName(ThreadGroup threadGroup) {
    return threadGroup == null ? "{null group}" : threadGroup.getName();
  }
}
