package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * Utility classes for selecting random numbers from within a range or the 
 * numeric domain for a given type.
 * 
 * @see BiasedNumbers
 */
public final class RandomNumbers {
  /** 
   * A random integer between <code>min</code> (inclusive) and <code>max</code> (inclusive).
   */
  public static int randomIntBetween(Random r, int min, int max) {
    assert max >= min : "max must be >= min: " + min + ", " + max;
    long range = (long) max - (long) min;
    if (range < Integer.MAX_VALUE) {
      return min + r.nextInt(1 + (int) range);
    } else {
      return toIntExact(min + nextLong(r, 1 + range));
    }
  }

  /** 
   * A random long between <code>min</code> (inclusive) and <code>max</code> (inclusive).
   */
  public static long randomLongBetween(Random r, long min, long max) {
    assert max >= min : "max must be >= min: " + min + ", " + max;
    long range = max - min;
    if (range < 0) {
      range -= Long.MAX_VALUE;
      if (range == Long.MIN_VALUE) {
        // Full spectrum.
        return r.nextLong();
      } else {
        long first = r.nextLong() & Long.MAX_VALUE;
        long second = range == Long.MAX_VALUE ? (r.nextLong() & Long.MAX_VALUE) : nextLong(r, range + 1);
        return min + first + second;
      }
    } else {
      long second = range == Long.MAX_VALUE ? (r.nextLong() & Long.MAX_VALUE) : nextLong(r, range + 1);
      return min + second; 
    }
  }

  /**
   * Similar to {@link Random#nextInt(int)}, but returns a long between
   * 0 (inclusive) and <code>n</code> (exclusive).
   * 
   * @param rnd Random generator.
   * @param n the bound on the random number to be returned.  Must be
   *        positive.
   * @return Returns a random number between 0 and n-1. 
   */
  public static long nextLong(Random rnd, long n) {
    if (n <= 0) {
      throw new IllegalArgumentException("n <= 0: " + n);
    }

    long value = rnd.nextLong();
    long range = n - 1;
    if ((n & range) == 0L) {
      value &= range;
    } else {
      for (long u = value >>> 1; u + range - (value = u % n) < 0L;) {
        u = rnd.nextLong() >>> 1;
      }
    }
    return value;
  }

  private static int toIntExact(long value) {
    if (value > Integer.MAX_VALUE) {
      throw new ArithmeticException("Overflow: " + value);
    } else {
      return (int) value;
    }
  }  
}
