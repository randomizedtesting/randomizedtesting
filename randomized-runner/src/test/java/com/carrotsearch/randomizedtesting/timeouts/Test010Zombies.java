package com.carrotsearch.randomizedtesting.timeouts;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.SysGlobals;
import com.carrotsearch.randomizedtesting.Utils;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakZombies;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakZombies.Consequence;
import com.carrotsearch.randomizedtesting.rules.SystemPropertiesRestoreRule;

public class Test010Zombies extends WithNestedTestClass {
  @ThreadLeakScope(Scope.TEST)
  @ThreadLeakLingering(linger = 0)
  @ThreadLeakAction({Action.INTERRUPT})
  @ThreadLeakZombies(Consequence.IGNORE_REMAINING_TESTS)
  public static class Nested extends ApplyAtPlace {}

  @Test public void testClassRule() { check(Place.CLASS_RULE); }
  @Test public void testBeforeClass() { check(Place.BEFORE_CLASS); }
  @Test public void testConstructor() { check(Place.CONSTRUCTOR); }
  @Test public void testTestRule() { check(Place.TEST_RULE); }
  @Test public void testBefore() { check(Place.BEFORE); }
  @Test public void testTest() { check(Place.TEST); }
  @Test public void testAfter() { check(Place.AFTER); }
  @Test public void testAfterClass() { check(Place.AFTER_CLASS); }

  @Rule
  public SystemPropertiesRestoreRule restoreProperties = new SystemPropertiesRestoreRule(); 

  /**
   * Start a zombie thread somewhere. Ensure all suites are ignored afterwards.
   */
  private void check(Place p) {
    System.setProperty(SysGlobals.SYSPROP_KILLWAIT(), "10");
    System.setProperty(SysGlobals.SYSPROP_KILLATTEMPTS(), "2");

    ApplyAtPlace.place = p;
    ApplyAtPlace.runnable = new Runnable() {
      @Override
      public void run() {
        startZombieThread("foobarZombie");
      }
    };

    // Run a class spawning zombie threads.
    FullResult r = runTests(Nested.class);

    Utils.assertFailureWithMessage(r, "1 thread leaked");
    Utils.assertFailureWithMessage(r, "foobarZombie");
    Utils.assertFailuresContainSeeds(r);

    Assertions.assertThat(getLoggingMessages())
      .contains("There are still zombie threads that couldn't be terminated:");

    // Run another suite. Everything should be ignored because of zombie threads.
    for (Place p2 : Place.values()) {
      ApplyAtPlace.place = p2;
      ApplyAtPlace.runnable = new Runnable() {
        @Override
        public void run() {
          throw new RuntimeException();
        }
      };

      r = runTests(Nested.class);
      Assertions.assertThat(r.wasSuccessful()).as("At: " + p2).isTrue();
    }
  }
}

