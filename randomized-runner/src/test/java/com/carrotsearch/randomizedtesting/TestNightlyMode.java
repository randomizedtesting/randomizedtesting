package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    dailyDefault();
  }

  @Test
  public void nightly() {
    System.setProperty(TestGroup.Utilities.getSysProperty(Nightly.class), "yes");
    checkTestsOutput(2, 0, 0, 0, Nested.class);
  }

  @Test
  public void dailyDefault() {
    checkTestsOutput(2, 0, 1, 1, Nested.class);
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
