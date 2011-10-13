package com.carrotsearch.randomizedtesting.examples.barcelona;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

public class TestMathAbs extends RandomizedTest {
  @Test
  // @Seed("487a51b") // get unlucky...
  public void absoluteWeirdness() {
    for (int i = 0; i < 100; i++) {
      assertTrue(Math.abs(randomInt()) >= 0);
    }
  }
}
