package com.carrotsearch.randomizedtesting;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Timeout;

/**
 * Test {@link Test#timeout()}.
 */
public class TestTimeout extends WithNestedTestClass {
  @Timeout(millis = 0)
  public static class Nested extends RandomizedTest {
    @Test(timeout = 100)
    public void testMethod1() {
      assumeRunningNested();
      sleep(2000);
    }

    @Test(timeout = 100)
    public void testMethod2() {
      assumeRunningNested();
      long start = System.currentTimeMillis();
      while (System.currentTimeMillis() - start < 2000) {
        // busy loop.
      }
    }
  }

  @Test
  public void testTimeoutInTestAnnotation() {
    Result result = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(0, result.getIgnoreCount());
    Assert.assertEquals(2, result.getRunCount());
    Assert.assertEquals(2, result.getFailureCount());
  }
}
