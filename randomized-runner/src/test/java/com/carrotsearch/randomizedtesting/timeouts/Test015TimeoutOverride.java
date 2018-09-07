package com.carrotsearch.randomizedtesting.timeouts;

import org.junit.After;
import org.junit.Test;
import org.junit.Assert;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.SysGlobals;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.Timeout;


/**
 * Test global timeout override (-Dtests.timeout=1000!).
 */
public class Test015TimeoutOverride extends WithNestedTestClass {
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
    FullResult result = runTests(Nested.class);
    long end = System.currentTimeMillis();
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertTrue(end - start < 3000);
  }
  
  @Test
  public void testDisableTimeout() {
    System.setProperty(SysGlobals.SYSPROP_TIMEOUT(), "0!");

    long start = System.currentTimeMillis();
    FullResult result = runTests(Nested2.class);
    long end = System.currentTimeMillis();
    Assert.assertEquals(0, result.getFailureCount());
    Assert.assertTrue(end - start > 900);
  }
  
  @After
  public void cleanup() {
    System.clearProperty(SysGlobals.SYSPROP_TIMEOUT());
  }
}
