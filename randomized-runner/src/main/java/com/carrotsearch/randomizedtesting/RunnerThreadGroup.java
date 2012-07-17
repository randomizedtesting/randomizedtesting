package com.carrotsearch.randomizedtesting;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ThreadGroup} under which all tests (and hooks) are executed. Theoretically, there
 * should be no thread outside of this group's control.
 */
final class RunnerThreadGroup extends ThreadGroup {
  private final UncaughtExceptionHandler handler;

  /* */
  RunnerThreadGroup(String name, UncaughtExceptionHandler handler) {
    super(name);
    this.handler = handler;
  }

  /**
   * Capture all uncaught exceptions from this group's threads. 
   */
  @Override
  public void uncaughtException(Thread t, Throwable e) {
    // Try to get the context for this thread and augment the exception with the seed.
    try {
      e = RandomizedRunner.augmentStackTrace(e);
    } catch (Throwable ignore) {
      // if there's none, don't panic, but this is weird and should not happen.
      Logger.getLogger(RunnerThreadGroup.class.getSimpleName())
        .log(Level.SEVERE,
            RunnerThreadGroup.class.getSimpleName() + "'s sub thread should " +
            "always have a context and it didn't have any?", ignore);
    }

    handler.uncaughtException(t, e);
  }
}
