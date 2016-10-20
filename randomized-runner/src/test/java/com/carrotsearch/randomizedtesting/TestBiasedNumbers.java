package com.carrotsearch.randomizedtesting;

import java.util.Locale;
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
    sanityCheck(r, 0f, 0);
    sanityCheck(r, 0f, 1);
    sanityCheck(r, -1f, 1);
    sanityCheck(r, 1f, 2);
  }

  private void sanityCheck(Random r, float from, float to) {
    for (int i = 0; i < 100; i++) {
      float v = BiasedNumbers.randomFloatBetween(r, from, to);
      Assertions.assertThat(v).isNotNaN();
      Assertions.assertThat(v >= from && v <= to).isTrue();
    }
  }

  @Test
  @Repeat(iterations = 100)
  public void biasedDoubles() {
    Random r = getRandom();

    // Some sanity checks.
    sanityCheck(r, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    sanityCheck(r, Double.NEGATIVE_INFINITY, 0);
    sanityCheck(r, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    sanityCheck(r, 0, Double.POSITIVE_INFINITY);
    sanityCheck(r, 0d, 0);
    sanityCheck(r, 0d, 1);
    sanityCheck(r, -1d, 1);
    sanityCheck(r, 1d, 2);
  }

  private void sanityCheck(Random r, double from, double to) {
    for (int i = 0; i < 100; i++) {
      double v = BiasedNumbers.randomDoubleBetween(r, from, to);
      Assertions.assertThat(v).isNotNaN();
      Assertions.assertThat(v >= from && v <= to).isTrue();
    }
  }
  
  @Test
  public void histo() {
    Random rnd = getRandom();
    int r = 10;
    int [][] counts = new int [r + 1][r + 1];
    for (int i = 0; i < 100000; i++) {
      float x = r / 2 + BiasedNumbers.randomFloatBetween(rnd, -r/2, r/2);
      float y = r / 2 + BiasedNumbers.randomFloatBetween(rnd, -r/2, r/2);

      counts[Math.round(x)][Math.round(y)]++;
    }
    
    for (int x = 0; x < counts.length; x++) {
      for (int y = 0; y < counts.length; y++) {
        System.out.printf(Locale.ROOT, "%10d ", counts[x][y]);
      } 
      System.out.println();
    }
  }
}
