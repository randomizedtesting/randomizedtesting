package com.carrotsearch.randomizedtesting;

import java.util.Random;

import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * Common scaffolding for subclassing randomized tests. 
 */
@RunWith(RandomizedRunner.class)
public class RandomizedTest extends Assert {
  /**
   * Shortcut for {@link RandomizedContext#current()}. 
   */
  protected static RandomizedContext getContext() {
    return RandomizedContext.current();
  }

  /**
   * Shortcut for {@link RandomizedContext#getRandom()}. Even though this method
   * is static, it return per-thread {@link Random} instance, so no race conditions
   * can occur.
   * 
   * <p>It is recommended that specific methods are used to pick random values.
   */
  protected static Random getRandom() {
    return getContext().getRandom();
  }

  // Random value pickers. Shortcuts to methods in {@link #getRandom()} mostly.  

  public static boolean randomBoolean()  { return getRandom().nextBoolean();  }
  public static byte    randomByte()     { return (byte) getRandom().nextInt(); }
  public static short   randomShort()    { return (short) getRandom().nextInt(); }
  public static int     randomInt()      { return getRandom().nextInt(); }
  public static float   randomFloat()    { return getRandom().nextFloat(); }
  public static double  randomDouble()   { return getRandom().nextDouble(); }
  public static long    randomLong()     { return getRandom().nextLong(); }
  public static double  randomGaussian() { return getRandom().nextGaussian(); }

  /** {@link Random#nextInt(int)}. */
  public static int     randomInt(int max) { return getRandom().nextInt(max); }

  /** A random integer from <code>min</code> to <code>max</code> (inclusive). */
  public static int     randomBetween(int min, int max) {
    assert max >= min; // TODO: overflows?
    return min + randomInt(max - min); 
  }
}
