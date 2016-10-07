package com.carrotsearch.randomizedtesting;

import org.junit.*;
import org.junit.runner.*;

/**
 * Test {@link Result}'s run count for ignored tests.
 */
public class TestIgnoredRunCount extends WithNestedTestClass {
  public static class Nested {
    @Test @Ignore
    public void ignored() {
    }
  }

  @Test
  public void checkIgnoredCount() throws Exception {
    assertSameExecution(Nested.class);
  }

  private void assertSameExecution(Class<?> clazz) throws Exception {
    Result result1 = runClasses(clazz);
    Result result2 = new JUnitCore().run(Request.runner(new RandomizedRunner(clazz)));

    Assert.assertEquals(result1.getRunCount(), result2.getRunCount());
    Assert.assertEquals(result1.getFailureCount(), result2.getFailureCount());
    Assert.assertEquals(result1.getIgnoreCount(), result2.getIgnoreCount());
  }
}
