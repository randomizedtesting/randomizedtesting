package com.carrotsearch.randomizedtesting.timeouts;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.LifecycleScope;
import com.carrotsearch.randomizedtesting.Utils;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

public class Test003ThreadLeaksMethod extends WithNestedTestClass {
  @ThreadLeakScope(Scope.TEST)
  @ThreadLeakLingering(linger = 0)
  @ThreadLeakAction({Action.WARN, Action.INTERRUPT})
  public static class Nested extends ApplyAtPlace {}

  @Test public void testClassRule() { suiteLeak(Place.CLASS_RULE); }
  @Test public void testBeforeClass() { suiteLeak(Place.BEFORE_CLASS); }
  @Test public void testConstructor() { suiteLeak(Place.CONSTRUCTOR); }
  @Test public void testTestRule() { testLeak(Place.TEST_RULE); }
  @Test public void testBefore() { testLeak(Place.BEFORE); }
  @Test public void testTest() { testLeak(Place.TEST); }
  @Test public void testAfter() { testLeak(Place.AFTER); }
  @Test public void testAfterClass() { suiteLeak(Place.AFTER_CLASS); }

  private void testLeak(Place p) {
    check(p, LifecycleScope.TEST);
  }

  private void suiteLeak(Place p) {
    check(p, LifecycleScope.SUITE);
  }

  /**
   * Check a given timeout place. 
   */
  private void check(Place p, LifecycleScope scope) {
    ApplyAtPlace.place = p;
    ApplyAtPlace.runnable = new Runnable() {
      @Override
      public void run() {
        startThread("foobar");
      }
    };

    FullResult r = runTests(Nested.class);
    Utils.assertFailureWithMessage(r, "1 thread leaked from " + scope.toString() + " scope at");
    Assert.assertEquals(1, r.getFailureCount());
    Utils.assertFailuresContainSeeds(r);
    Utils.assertNoLiveThreadsContaining("foobar");

    Assertions.assertThat(getLoggingMessages())
      .doesNotContain("Uncaught exception");
  }
}

