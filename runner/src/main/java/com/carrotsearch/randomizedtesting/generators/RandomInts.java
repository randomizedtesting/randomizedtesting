package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * Utility classes for random integer and integer sequences.
 */
public final class RandomInts {
  /** 
   * A random integer from <code>min</code> to <code>max</code> (inclusive).
   */
  public static int     randomIntBetween(Random r, int min, int max) {
    assert max >= min; // TODO: overflows?
    return min + r.nextInt(max - min + 1); 
  }

  /**
   * A random integer between 0 and <code>max</code> (inclusive).
   */
  public static int randomInt(Random r, int max) {
    // TODO: assert range?
    return r.nextInt(max + 1);
  }
}
