package com.carrotsearch.randomizedtesting.snippets;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

//[[[start:goodbadugly]]] 
public class TheGoodBadAndUgly extends RandomizedTest {
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
//[[[end:goodbadugly]]]

