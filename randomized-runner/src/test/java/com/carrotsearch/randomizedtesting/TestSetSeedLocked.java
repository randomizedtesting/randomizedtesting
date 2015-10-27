package com.carrotsearch.randomizedtesting;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestSetSeedLocked extends RandomizedTest {
  @Test
  public void testMethod() {
    try {
      getRandom().setSeed(0);
      fail();
    } catch (RuntimeException e) {
      // Ok, expected.
    }
  }
}
