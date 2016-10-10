package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertFalse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Hooks should _not_ execute if there are no test cases to run. Note that
 * ignored test cases behave as if there was something to execute (!).
 */
public class TestHooksWithEmptyTestSet extends WithNestedTestClass {
  @RunWith(RandomizedRunner.class)
  public static class Nested {
    static boolean beforeClassExecuted;
    static boolean afterClassExecuted;

    @BeforeClass
    public static void beforeClass() {
      assumeRunningNested();
      beforeClassExecuted = true;
    }

    @AfterClass
    public static void afterClass() {
      afterClassExecuted = true;
    }    
  }
  
  /**
   * Check if methods get the same seed on every run with a fixed runner's seed.
   */
  @Test
  public void testSameMethodRandomnessWithFixedRunner() {
    Nested.beforeClassExecuted = false;
    Nested.afterClassExecuted = false;
    checkTestsOutput(0, 0, 0, 0, Nested.class);
    assertFalse(Nested.beforeClassExecuted);
    assertFalse(Nested.afterClassExecuted);
  }
}
