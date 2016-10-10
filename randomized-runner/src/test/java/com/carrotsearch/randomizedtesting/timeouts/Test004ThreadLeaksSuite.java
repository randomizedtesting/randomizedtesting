package com.carrotsearch.randomizedtesting.timeouts;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.Utils;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

public class Test004ThreadLeaksSuite extends WithNestedTestClass {
  @ThreadLeakScope(Scope.SUITE)
  @ThreadLeakLingering(linger = 0)
  @ThreadLeakAction({Action.WARN, Action.INTERRUPT})
  public static class Nested extends ApplyAtPlace {}

  @Test public void testClassRule() { suiteLeak(Place.CLASS_RULE); }
  @Test public void testBeforeClass() { suiteLeak(Place.BEFORE_CLASS); }
  @Test public void testConstructor() { suiteLeak(Place.CONSTRUCTOR); }
  @Test public void testTestRule() { suiteLeak(Place.TEST_RULE); }
  @Test public void testBefore() { suiteLeak(Place.BEFORE); }
  @Test public void testTest() { suiteLeak(Place.TEST); }
  @Test public void testAfter() { suiteLeak(Place.AFTER); }
  @Test public void testAfterClass() { suiteLeak(Place.AFTER_CLASS); }

  /**
   * Check a given timeout place. 
   */
  private void suiteLeak(Place p) {
    ApplyAtPlace.place = p;
    ApplyAtPlace.runnable = new Runnable() {
      @Override
      public void run() {
        startThread("foobar");
      }
    };

    FullResult r = runTests(Nested.class);
    Utils.assertFailureWithMessage(r, "1 thread leaked from SUITE scope at");
    Assert.assertEquals(1, r.getFailureCount());
    Utils.assertFailuresContainSeeds(r);
    Utils.assertNoLiveThreadsContaining("foobar");
    
    Assertions.assertThat(getLoggingMessages())
      .doesNotContain("Uncaught exception");    
  }
}

