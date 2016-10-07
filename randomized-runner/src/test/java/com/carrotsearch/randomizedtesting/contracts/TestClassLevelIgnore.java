package com.carrotsearch.randomizedtesting.contracts;

import org.junit.*;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

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
    Result result = runClasses(Nested.class);
    Assert.assertEquals(0, result.getRunCount());
    Assert.assertEquals(0, result.getFailureCount());
    Assert.assertEquals(1, result.getIgnoreCount());
  }
}
