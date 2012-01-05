package com.carrotsearch.examples.randomizedtesting;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class TestFiftyPercentFailure extends RandomizedTest {
  /**
   * About 50% out of fifty executions of this test case will fail.
   */
  @Test
  @Repeat(iterations = 50)
  public void fiftyFifty() {
    assertTrue(randomBoolean());
  }
}
