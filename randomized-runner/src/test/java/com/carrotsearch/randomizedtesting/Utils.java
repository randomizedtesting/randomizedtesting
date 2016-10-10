package com.carrotsearch.randomizedtesting;

import java.util.Set;
import java.util.logging.Logger;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.carrotsearch.randomizedtesting.WithNestedTestClass.FullResult;

public class Utils {
  /**
   * Assert a result has at least one failure with message.
   */
  public static void assertFailureWithMessage(FullResult r, String message) {
    for (Failure f : r.getFailures()) {
      if (f.getTrace().contains(message)) {
        return;
      }
    }

    StringBuilder b = new StringBuilder("No failure with message: '" + message + "' (" +
        r.getFailures().size() + " failures):");
    for (Failure f : r.getFailures()) {
      b.append("\n\t- ").append(f.getTrace());
    }
    Logger.getLogger("").severe(b.toString());
    Assert.fail(b.toString());
  }

  public static void assertNoFailureWithMessage(Result r, String message) {
    boolean hadMessage = false;
    for (Failure f : r.getFailures()) {
      if (f.getTrace().contains(message)) {
        hadMessage = true;
      }
    }
    
    if (!hadMessage) return;

    StringBuilder b = new StringBuilder("Failure with message: '" + message + "' (" +
        r.getFailures().size() + " failures):");
    for (Failure f : r.getFailures()) {
      b.append("\n\t- ").append(f.getTrace());
    }
    Logger.getLogger("").severe(b.toString());
    Assert.fail(b.toString());
  }

  /**
   * Check that all thrown failures have been augmented and contain
   * a synthetic seed frame.
   */
  public static void assertFailuresContainSeeds(FullResult r) {
    for (Failure f : r.getFailures()) {
      String seed = RandomizedRunner.seedFromThrowable(f.getException());
      Assert.assertTrue("Not augmented: " + f.getTrace(), seed != null);
    }
  }

  /**
   * Package scope test access to {@link RandomizedContext#getRunnerSeed()}.
   */
  public static long getRunnerSeed() {
    return RandomizedContext.current().getRunnerSeed();
  }

  /**
   * Package scope test access to {@link Randomness#getSeed()}. 
   */
  public static long getSeed(Randomness randomness) {
    return randomness.getSeed();
  }

  /**
   * Assert no threads with the given substring are present.
   */
  public static void assertNoLiveThreadsContaining(String substring) {
    for (Thread t : getAllThreads()) {
      if (t.isAlive()) {
        Assertions.assertThat(t.getName())
          .as("Unexpected live thread").doesNotContain(substring);
      }
    }
  }

  /**
   * Expose to non-package scope. 
   */
  public static Set<Thread> getAllThreads() {
    return Threads.getAllThreads();
  }
  
  /**
   * Expose to non-package scope. 
   */
  public static ThreadGroup getTopThreadGroup() {
    return Threads.getTopThreadGroup();
  }  
}
