package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.TestGroup;

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
    System.setProperty(TestGroup.Utilities.getSysProperty(Nightly.class), "invalid-value");
    checkRunClasses(1, 0, 1, Nested.class);
  }

  @Test
  public void nightly() {
    System.setProperty(TestGroup.Utilities.getSysProperty(Nightly.class), "yes");
    Result result = runClasses(Nested.class);
    Assert.assertEquals(2, result.getRunCount());
    Assert.assertEquals(0, result.getFailureCount());
  }

  @Test
  public void dailyDefault() {
    JUnitCore core = new JUnitCore();
    core.addListener(new PrintEventListener());
    Result result = core.run(Nested.class);
    
    Assert.assertEquals(2, result.getRunCount());
    Assert.assertEquals(1, result.getFailureCount());
    Assert.assertEquals(0, result.getIgnoreCount());
  }

  @Before
  public void cleanupBefore() {
    cleanupAfter();
  }
  
  @After
  public void cleanupAfter() {
    System.clearProperty(TestGroup.Utilities.getSysProperty(Nightly.class));
  }  
}
