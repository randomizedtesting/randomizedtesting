package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * Utility classes for random integer and integer sequences.
 */
public final class RandomInts {
  /** 
   * A random integer from <code>min</code> to <code>max</code> (inclusive).
   */
  public static int randomIntBetween(Random r, int min, int max) {
    assert max >= min : "max must be >= min: " + min + ", " + max;
    long range = (long) max - (long) min;
    if (range < Integer.MAX_VALUE) {
      return min + r.nextInt(1 + (int) range);
    } else {
      return min + (int) Math.round(r.nextDouble() * range);
    }
  }

  /**
   * A random integer between 0 and <code>max</code> (inclusive).
   */
  public static int randomInt(Random r, int max) {
    if (max == 0)
      return 0;
    else if (max == Integer.MAX_VALUE)
      return r.nextInt() & 0x7fffffff;
    else
      return r.nextInt(max + 1);
  }
}
