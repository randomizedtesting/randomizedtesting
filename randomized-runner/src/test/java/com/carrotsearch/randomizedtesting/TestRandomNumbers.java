package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.generators.RandomNumbers;

@Repeat(iterations = 100)
public class TestRandomNumbers extends RandomizedTest {
  @Test
  public void testRandomIntBetween() {
    int max = Integer.MAX_VALUE;
    int min = Integer.MIN_VALUE;

    Random rnd = getRandom();
    checkRandomInt(rnd, 1, 1);
    checkRandomInt(rnd, 0, 100);
    checkRandomInt(rnd, 0, max);
    checkRandomInt(rnd, min, 0);
    checkRandomInt(rnd, -1, max);
    checkRandomInt(rnd, min, 1);
    checkRandomInt(rnd, min, max);
  }

  @Test
  public void testRandomLongBetween() {
    long max = Long.MAX_VALUE;
    long min = Long.MIN_VALUE;

    Random rnd = getRandom();
    checkRandomLong(rnd, 1, 1);
    checkRandomLong(rnd, 0, 100);
    checkRandomLong(rnd, 0, max);
    checkRandomLong(rnd, min, 0);
    checkRandomLong(rnd, -1, max);
    checkRandomLong(rnd, min, 1);
    checkRandomLong(rnd, min, max);
  }

  private void checkRandomInt(Random rnd, int min, int max) {
    for (int i = 0; i < 100000; i++) {
      int v = RandomNumbers.randomIntBetween(rnd, min, max);
      if (v < min || v > max) {
        fail(min + " " + max + ": " + v);
      }
    }
  }
  
  private void checkRandomLong(Random rnd, long min, long max) {
    for (int i = 0; i < 100000; i++) {
      long v = RandomNumbers.randomLongBetween(rnd, min, max);
      if (v < min || v > max) {
        fail(min + " " + max + ": " + v);
      }
    }
  }  
}
