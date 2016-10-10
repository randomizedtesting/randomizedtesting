package com.carrotsearch.randomizedtesting.timeouts;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.Utils;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.Timeout;

public class Test002TimeoutMethod extends WithNestedTestClass {
  @Timeout(millis = 25)
  public static class Nested extends ApplyAtPlace {}

  @Test public void testTestRule() { check(Place.TEST_RULE); }
  @Test public void testBefore() { check(Place.BEFORE); }
  @Test public void testTest() { check(Place.TEST); }
  @Test public void testAfter() { check(Place.AFTER); }

  /**
   * Check a given timeout place. 
   */
  private void check(Place p) {
    ApplyAtPlace.place = p;
    ApplyAtPlace.runnable = new Runnable() {
      @Override
      public void run() {
        while (true) RandomizedTest.sleep(1000);
      }
    };

    FullResult r = runTests(Nested.class);
    Utils.assertFailureWithMessage(r, "Test timeout exceeded");
    Utils.assertFailuresContainSeeds(r);
  }
}

