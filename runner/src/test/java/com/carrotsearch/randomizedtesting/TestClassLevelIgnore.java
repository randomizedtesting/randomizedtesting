package com.carrotsearch.randomizedtesting;

import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Class-level {@link Ignore}.
 */
public class TestClassLevelIgnore extends WithNestedTestClass {
  @Ignore
  public static class Nested extends RandomizedTest {
    @Test
    public void ignored() {
    }
  }

  @Test
  public void allIgnored() {
    Result result = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(0, result.getRunCount());
    Assert.assertEquals(0, result.getFailureCount());
    Assert.assertEquals(1, result.getIgnoreCount());
  }
}
