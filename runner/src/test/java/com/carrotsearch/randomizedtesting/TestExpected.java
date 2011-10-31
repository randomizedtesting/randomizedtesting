package com.carrotsearch.randomizedtesting;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Test {@link Test#expected()}.
 */
public class TestExpected extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Test(expected = RuntimeException.class)
    public void testMethod1() {
      throw new RuntimeException();
    }
    
    // We expect a RuntimeException but get an error: should fail.
    @Test(expected = RuntimeException.class)
    public void testMethod2() {
      assumeRunningNested();
      throw new Error();
    }
  }

  @Test
  public void testSameMethodRandomnessWithFixedRunner() {
    Result result = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(0, result.getIgnoreCount());
    Assert.assertEquals(2, result.getRunCount());
    Assert.assertEquals(1, result.getFailureCount());
    
    Assert.assertSame(Error.class, result.getFailures().get(0).getException()
        .getClass());
  }
}
