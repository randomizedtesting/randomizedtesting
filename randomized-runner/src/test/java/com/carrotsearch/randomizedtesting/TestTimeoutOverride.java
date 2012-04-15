package com.carrotsearch.randomizedtesting;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Timeout;

/**
 * Test global timeout override (-Dtests.timeout=1000!).
 */
public class TestTimeoutOverride extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Test
    @Timeout(millis = 5000)
    public void testMethod1() {
      assumeRunningNested();
      sleep(10000);
    }
  }

  public static class Nested2 extends RandomizedTest {
    @Test
    @Timeout(millis = 100)
    public void testMethod1() {
      assumeRunningNested();
      sleep(1000);
    }
  }

  @Test
  public void testTimeoutOverride() {
    System.setProperty(SysGlobals.SYSPROP_TIMEOUT(), "200!");
    long start = System.currentTimeMillis();
    Result result = JUnitCore.runClasses(Nested.class);
    long end = System.currentTimeMillis();
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertTrue(end - start < 3000);
  }
  
  @Test
  public void testDisableTimeout() {
    System.setProperty(SysGlobals.SYSPROP_TIMEOUT(), "0!");

    long start = System.currentTimeMillis();
    Result result = JUnitCore.runClasses(Nested2.class);
    long end = System.currentTimeMillis();
    Assert.assertEquals(0, result.getFailureCount());
    Assert.assertTrue(end - start > 900);
  }
}
