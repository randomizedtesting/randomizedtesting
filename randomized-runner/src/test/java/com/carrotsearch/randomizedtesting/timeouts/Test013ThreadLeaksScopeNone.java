package com.carrotsearch.randomizedtesting.timeouts;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

public class Test013ThreadLeaksScopeNone extends WithNestedTestClass {
  @ThreadLeakScope(Scope.NONE)
  public static class Nested extends ApplyAtPlace {}

  @Test public void testClassRule() { testLeak(Place.CLASS_RULE); }
  @Test public void testBeforeClass() { testLeak(Place.BEFORE_CLASS); }
  @Test public void testConstructor() { testLeak(Place.CONSTRUCTOR); }
  @Test public void testTestRule() { testLeak(Place.TEST_RULE); }
  @Test public void testBefore() { testLeak(Place.BEFORE); }
  @Test public void testTest() { testLeak(Place.TEST); }
  @Test public void testAfter() { testLeak(Place.AFTER); }
  @Test public void testAfterClass() { testLeak(Place.AFTER_CLASS); }

  private void testLeak(Place p) {
    ApplyAtPlace.place = p;
    ApplyAtPlace.runnable = new Runnable() {
      @Override
      public void run() {
        startZombieThread("foobar");
      }
    };

    FullResult r = runTests(Nested.class);
    Assert.assertEquals(0, r.getFailureCount());

    Assertions.assertThat(getLoggingMessages()).isEmpty();
    Assertions.assertThat(getSysouts()).isEmpty();
  }
}
