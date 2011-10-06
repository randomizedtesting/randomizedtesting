package com.carrotsearch.randomizedtesting;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Just an eyeballing test at the output ;)
 */
// Global override: -Drandomized.seed=runner:tests
// Global override: -Drandomized.iter=X

// Fix all starting seeds with: @Seed("runner:tests")
@RunWith(RandomizedRunner.class)
public class TestEyeBalling {
  @Repeat(iterations = 4)
  @Test
  public void repeatThreeTimes() {
    // do nothing.
  }

  @Seed("456bdce85147941")
  @Repeat(iterations = 3, useConstantSeed = true)
  @Test
  public void repeatThreeTimesFixedStartSeed() {
    // do nothing.
  }

  @Test
  public void repeatOneTime() {
    // do nothing.
  }
}

