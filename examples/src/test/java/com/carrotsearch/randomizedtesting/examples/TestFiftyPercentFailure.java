package com.carrotsearch.randomizedtesting.examples;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.Repeat;

import static org.junit.Assert.*;

@RunWith(RandomizedRunner.class)
public class TestFiftyPercentFailure {
  /**
   * About 50% out of fifty executions of this test case will fail.
   */
  @Test
  @Repeat(iterations = 50)
  public void fiftyFifty() {
    assertTrue(RandomizedContext.current().getRandom().nextBoolean());
  }
}
