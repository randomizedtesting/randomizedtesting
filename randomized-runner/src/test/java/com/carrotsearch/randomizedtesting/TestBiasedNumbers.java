package com.carrotsearch.randomizedtesting;

import java.util.Random;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.generators.BiasedNumbers;

public class TestBiasedNumbers extends RandomizedTest {
  @Test
  @Repeat(iterations = 100)
  public void biasedFloats() {
    Random r = getRandom();

    // Some sanity checks.
    sanityCheck(r, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    sanityCheck(r, Float.NEGATIVE_INFINITY, 0);
    sanityCheck(r, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    sanityCheck(r, 0, Float.POSITIVE_INFINITY);
    sanityCheck(r, 0, 0);
    sanityCheck(r, 0, 1);
    sanityCheck(r, -1, 1);
  }

  private void sanityCheck(Random r, float from, float to) {
    for (int i = 0; i < 100; i++) {
      float v = BiasedNumbers.randomFloatBetween(r, from, to);
      Assertions.assertThat(v).isNotNaN();
      Assertions.assertThat(v >= from && v <= to).isTrue();
    }
  }
}
