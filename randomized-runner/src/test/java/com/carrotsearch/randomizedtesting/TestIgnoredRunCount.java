package com.carrotsearch.randomizedtesting;

import org.junit.*;
import org.junit.runner.*;

/**
 * Test {@link Result}'s run count for ignored tests.
 */
public class TestIgnoredRunCount extends WithNestedTestClass {
  public static class Nested1 {
    @Test @Ignore
    public void ignored() {}
  }

  @RunWith(RandomizedRunner.class)
  public static class Nested2 {
    @Test @Ignore
    public void ignored() {}
  }

  @Test
  public void checkIgnoredCount() throws Exception {
    checkTestsOutput(0, 1, 0, 0, Nested1.class);
    checkTestsOutput(0, 1, 0, 0, Nested2.class);
  }
}
