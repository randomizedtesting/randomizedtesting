package com.carrotsearch.randomizedtesting;

import org.junit.Test;
import org.junit.jupiter.api.RepeatedTest;

public class TestUsesVintageRunner extends RandomizedTest {
  @Test
  public void usesVintageTestRunner() {
    System.out.println(
        "test: vintage runner, seed: " + RandomizedContext.current().getRandomness());
  }

  @RepeatedTest(3)
  public void usesJupiterInAVintageRunnerClass() {
    System.out.println("test: jupiter in a vintage class.");
  }
}
