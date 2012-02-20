package com.carrotsearch.randomizedtesting.snippets;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TheGoodBadAndUglySnippet extends WithNestedTestClass {
  
  /**
   * This is cheating so that snippets appear to be extending RandomizedTest
   * but at the same time snippety tests won't execute in Eclipse.
   */
  public static class RandomizedTest extends com.carrotsearch.randomizedtesting.RandomizedTest {
    @Before
    public void before() {
      assumeTrue("Ignore snippets.", false);
    }
  }
  
  // [[[start:goodbadugly]]]
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
      // I start and abandon a spinning thread outside the test scope.
      new Thread() {
        public void run() {
          while (true) {/* Spin. */}
        }
      }.start();
    }
  }
  // [[[end:goodbadugly]]]
}