package com.carrotsearch.randomizedtesting.examples.barcelona;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

public class TestExample2 extends RandomizedTest {
  @Test
  public void testNextInt() {
    int from = randomInt() & 0x7fffffff;
    int to   = randomIntBetween(from, 0x7fffffff);
    int value = nextInt(getRandom(), from, to);
    assertTrue("! " + from + " <= " + value + " <= to", 
        value >= from && value <= to);
  }

  // If you can find a seed that will result in a direct immediate sequence of 0, Integer.MAX_VALUE,
  // I will send you a bottle of a nice scotch :) Yes, this is a hidden gem. ;)
  // @Seed(?)
  @Test
  public void testNextIntFailing() {
    int from = 0;
    int to   = Integer.MAX_VALUE;
    int value = nextInt(getRandom(), from, to);
    assertTrue("! " + from + " <= " + value + " <= to", 
        value >= from && value <= to);
  }

  /** From: Lucene's _testUtil
   *  Start and end are BOTH inclusive. */
  public static int nextInt(Random r, int start, int end) {
    return start + r.nextInt(end - start + 1);
  }
}
