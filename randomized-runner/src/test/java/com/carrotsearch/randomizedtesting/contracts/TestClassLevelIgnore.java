package com.carrotsearch.randomizedtesting.contracts;

import org.junit.Ignore;
import org.junit.Test;

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
    checkTestsOutput(0, 1, 0, 0, Nested.class);
  }
}
