package com.carrotsearch.randomizedtesting;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Nightly;

/**
 * Nightly mode checks.
 */
public class TestNightlyMode extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Test
    public void passOnNightlyOnly() {
      assumeRunningNested();
      assertTrue(isNightly());
    }

    @Test @Nightly("Nightly only test case.")
    public void nightlyOnly() throws Exception {
    }
  }

  @Test
  public void invalidValueNightly() {
    System.setProperty(RuntimeTestGroup.getGroupSysProperty(Nightly.class), "invalid-value");
    Result result = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(2, result.getRunCount());
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertEquals(1, result.getIgnoreCount());
  }

  @Test
  public void nightly() {
    System.setProperty(RuntimeTestGroup.getGroupSysProperty(Nightly.class), "yes");
    Result result = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(2, result.getRunCount());
    Assert.assertEquals(0, result.getFailureCount());
  }

  @Test
  public void dailyDefault() {
    Result result = JUnitCore.runClasses(Nested.class);
    Assert.assertEquals(2, result.getRunCount());
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertEquals(1, result.getIgnoreCount());
  }

  @Before
  public void cleanupBefore() {
    cleanupAfter();
  }
  
  @After
  public void cleanupAfter() {
    System.clearProperty(RuntimeTestGroup.getGroupSysProperty(Nightly.class));
  }
}
