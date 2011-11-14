package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

/**
 * {@link RandomizedRunner} can augment stack traces to include seed info. Check
 * if it works.
 */
public class TestStackAugmentation extends WithNestedTestClass {
  @RunWith(RandomizedRunner.class)
  public static class Nested {
    @Test
    public void testMethod1() {
      assumeRunningNested();

      // Throws a chained exception.
      try {
        throw new RuntimeException("Inner.");
      } catch (Exception e) {
        throw new Error("Outer.", e);
      }
    }
  }

  /**
   * Check if methods get the same seed on every run with a fixed runner's seed.
   */
  @Test
  public void testSameMethodRandomnessWithFixedRunner() {
    Result result = JUnitCore.runClasses(Nested.class);
    assertEquals(1, result.getFailureCount());
    
    Failure f = result.getFailures().get(0);
    assertNotNull(RandomizedRunner.seedFromThrowable(f.getException()));
  }
}
