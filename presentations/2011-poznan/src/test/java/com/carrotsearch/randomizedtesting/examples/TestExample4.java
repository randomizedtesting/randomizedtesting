package com.carrotsearch.randomizedtesting.examples;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

public class TestExample4 extends RandomizedTest {
  @Nightly @Test @Repeat(iterations = 5)
  public void testNightly() {
    // sleep :)
  }

  @Test @Repeat(iterations = 5)
  public void testAlways() {
    assertTrue(randomBoolean());
  }
  
  @Seed("deadbeef")
  @Test @Repeat(iterations = 5, useConstantSeed = true)
  public void testAlwaysFail() {
    assertTrue(randomBoolean());
  }    
}

