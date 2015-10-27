package com.carrotsearch.randomizedtesting.snippets;

import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

import static org.junit.Assert.*;

public class TheGoodBadAndUglySnippet extends WithNestedTestClass {
  /**
   * This is cheating so that snippets appear to be extending RandomizedTest
   * but at the same time snippety tests won't execute in Eclipse.
   */
  public static class RandomizedTest extends com.carrotsearch.randomizedtesting.RandomizedTest {
    @BeforeClass
    public static void runAsTest() {
      assumeRunningNested();
    }
  }

  // [[[start:goodbadugly]]]
  @ThreadLeakScope(Scope.TEST)
  @ThreadLeakAction({Action.WARN, Action.INTERRUPT})
  @ThreadLeakLingering(linger = 1000)
  public static class TheGoodBadAndUgly extends RandomizedTest {
    @Test
    public void good() {
      // I do nothing and I'm good.
    }

    @Test
    @Repeat(iterations = 10)
    public void bad() {
      // I fail randomly, about 20% of the time.
      assertFalse(randomIntBetween(1, 100) <= 20);
    }

    @Test
    public void ugly() {
      // I start and abandon a thread which falls 
      // outside the test's scope. The test will fail.
      new Thread() {
        public void run() {
          RandomizedTest.sleep(5000);
        }
      }.start();
    }
  }
  // [[[end:goodbadugly]]]
}