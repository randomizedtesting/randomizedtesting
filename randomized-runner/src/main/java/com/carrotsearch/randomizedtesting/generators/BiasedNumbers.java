package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * Utility classes for selecting numbers at random, but not necessarily
 * in an uniform way. The implementation will try to pick "evil" numbers
 * more often than uniform selection would. This includes exact range
 * boundaries, numbers very close to range boundaries, numbers very close
 * (or equal) to zero, etc.  
 * 
 * The exact method of selection is implementation-dependent and 
 * may change (if we find even more evil ways). 
 */
public final class BiasedNumbers {
  /** 
   * A random float between <code>min</code> (inclusive) and <code>max</code>
   * (inclusive). If you wish to have an exclusive range,
   * use {@link Math#nextAfter(float, double)} to adjust the range.
   * 
   * The code was inspired by GeoTestUtil from Apache Lucene.
   * 
   * @param min Left range boundary, inclusive. May be {@link Float#NEGATIVE_INFINITY}, but not NaN.
   * @param max Right range boundary, inclusive. May be {@link Float#POSITIVE_INFINITY}, but not NaN.
   */
  public static float randomFloatBetween(Random r, float min, float max) {
    assert max >= min : "max must be >= min: " + min + ", " + max;
    assert !Float.isNaN(min) && !Float.isNaN(max);

    final int EVIL_RANGE_LEFT = 2;
    final int EVIL_RANGE_RIGHT = 2;
    final int EVIL_VERY_CLOSE_RANGE_ENDS = 6;
    final int EVIL_ZERO_OR_NEAR = 4;
    final int EVIL_SIMPLE_PROPORTION = 3;
    final int EVIL_RANDOM_REPRESENTATION_BITS = 10;

    boolean hasZero = min <= 0 && max >= 0;

    int pick = r.nextInt(
        EVIL_RANGE_LEFT + 
        EVIL_RANGE_RIGHT +
        EVIL_VERY_CLOSE_RANGE_ENDS + 
        (hasZero ? EVIL_ZERO_OR_NEAR : 0) +  
        EVIL_SIMPLE_PROPORTION +
        EVIL_RANDOM_REPRESENTATION_BITS);

    // Exact range ends
    pick -= EVIL_RANGE_LEFT;
    if (pick < 0 || min == max) {
      return min;
    }

    pick -= EVIL_RANGE_RIGHT;
    if (pick < 0) {
      return max;
    }

    // If we're dealing with infinities, adjust them to discrete values.
    assert min != max;
    if (Float.isInfinite(min)) {
      min = Math.nextUp(min);
    }
    if (Float.isInfinite(max)) {
      max = Math.nextAfter(max, Double.NEGATIVE_INFINITY);
    }

    // Numbers "very" close to range ends. "very" means a few floating point 
    // representation steps (ulps) away.
    pick -= EVIL_VERY_CLOSE_RANGE_ENDS;
    if (pick < 0) {
      if (r.nextBoolean()) {
        return fuzzyUp(r, min, max);
      } else {
        return fuzzyDown(r, max, min);
      }
    }

    // Zero or near-zero values, if within the range.
    if (hasZero) {
      pick -= EVIL_ZERO_OR_NEAR;
      if (pick < 0) {
        int v = r.nextInt(4);
        if (v == 0) {
          return 0f;
        } else if (v == 1) {
          return -0.0f;
        } else if (v == 2) {
          return fuzzyDown(r, 0f, min);
        } else if (v == 3) {
          return fuzzyUp(r, 0f, max);
        }
      }
    }

    // Simple proportional selection.
    pick -= EVIL_SIMPLE_PROPORTION;
    if (pick < 0) {
      return (float) (min + (((double) max - min) * r.nextDouble())); 
    }

    // Random representation space selection. This will be heavily biased
    // and overselect from the set of tiny values, if they're allowed.
    pick -= EVIL_RANDOM_REPRESENTATION_BITS;
    if (pick < 0) {
      int from = toSortable(min);
      int to = toSortable(max);
      return fromSortable(RandomNumbers.randomIntBetween(r, from, to));
    }

    throw new RuntimeException("Unreachable.");
  }

  /**
   * Fuzzify the input value by decreasing it by a few ulps, but never past min. 
   */
  public static float fuzzyDown(Random r, float v, float min) {
    assert v >= min;
    for (int steps = RandomNumbers.randomIntBetween(r, 1, 10); steps > 0 && v > min; steps--) {
      v = Math.nextAfter(v, Double.NEGATIVE_INFINITY);
    }
    return v;
  }

  /**
   * Fuzzify the input value by increasing it by a few ulps, but never past max. 
   */
  static float fuzzyUp(Random r, float v, float max) {
    assert v <= max;
    for (int steps = RandomNumbers.randomIntBetween(r, 1, 10); steps > 0 && v < max; steps--) {
      v = Math.nextUp(v);
    }
    return v;
  }

  private static float fromSortable(int sortable) {
    return Float.intBitsToFloat(flip(sortable));
  }

  private static int toSortable(float value) {
    return flip(Float.floatToIntBits(value));
  }

  private static int flip(int floatBits) {
    return floatBits ^ (floatBits >> 31) & 0x7fffffff;
  }  
}
