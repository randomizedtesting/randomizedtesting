package com.carrotsearch.randomizedtesting;

/**
 * A {@link ThreadGroup} under which all tests (and hooks) are executed. Theoretically, there
 * should be no thread outside of this group's control.
 */
final class RunnerThreadGroup extends ThreadGroup {
  /* */
  RunnerThreadGroup(String name) {
    super(name);
  }

  /**
   * Capture all uncaught exceptions from this group's threads. 
   */
  @Override
  public void uncaughtException(Thread t, Throwable e) {
    // Try to get the context for this thread and augment the exception with the seed.
    try {
      e = RandomizedRunner.augmentStackTrace(e);
    } catch (IllegalArgumentException ignore) {
      // Very likely the randomized context has been destroyed. Don't try to augment the exception.
      e.addSuppressed(ignore);
    } catch (Throwable ignore) {
      e.addSuppressed(ignore);
    }

    super.uncaughtException(t, e);
  }
}
