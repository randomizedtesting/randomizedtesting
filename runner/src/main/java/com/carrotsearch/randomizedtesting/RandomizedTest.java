package com.carrotsearch.randomizedtesting;

import java.io.File;
import java.util.Random;

import org.junit.Assert;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.generators.RandomInts;
import com.carrotsearch.randomizedtesting.generators.RandomStrings;

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

  //
  // Random value pickers. Shortcuts to methods in {@link #getRandom()} mostly.
  //

  protected static boolean randomBoolean()  { return getRandom().nextBoolean();  }
  protected static byte    randomByte()     { return (byte) getRandom().nextInt(); }
  protected static short   randomShort()    { return (short) getRandom().nextInt(); }
  protected static int     randomInt()      { return getRandom().nextInt(); }
  protected static float   randomFloat()    { return getRandom().nextFloat(); }
  protected static double  randomDouble()   { return getRandom().nextDouble(); }
  protected static long    randomLong()     { return getRandom().nextLong(); }
  protected static double  randomGaussian() { return getRandom().nextGaussian(); }

  //
  // Delegates to RandomInts.
  //

  /** A random integer from 0..max (inclusive). */
  protected static int     randomInt(int max) { 
    return RandomInts.randomInt(getRandom(), max); 
  }

  /** A random integer from <code>min</code> to <code>max</code> (inclusive). */
  protected static int     randomIntBetween(int min, int max) {
    return RandomInts.randomIntBetween(getRandom(), min, max);
  }

  //
  // "multiplied" or scaled value pickers. These will be affected by global multiplier.
  //

  /** */
  protected static double multiplier() {
    throw new UnsupportedOperationException("not implemented");
  }

  // final int NUM_TERMS = (int) (1000*RANDOM_MULTIPLIER * (1+random.nextDouble()));
  // =>
  // NUM_TERMS = (int) (multiplier() * randomIntBetween(1000, 2000));
  // or
  // NUM_TERMS = scaledRandomBetween(1000, 2000);

  /**
   * Pick uniform random value between min..max and scale it using {@link #multiplier()}.
   * For example:
   * <pre>
   * NUM_TERMS = (int) (multiplier() * randomIntBetween(1000, 2000));
   * </pre>
   * is the same as:
   * <pre>
   * NUM_TERMS = multipliedRandomBetween(1000, 2000);
   * </pre>
   */
  protected static int multipliedRandomBetween(int min, int max) {
    return (int) (randomIntBetween(min, max) * multiplier());
  }

  // Methods to help with I/O and environment.  

  /**
   * Global temporary directory created for the duration of this class lifespan. If
   * multiple class loaders are used, there may be more global temp dirs, but it
   * shouldn't really be the case in practice.
   */
  protected static File globalTempDir() {
    // TODO: implement me. Synchronized, lazy-init.
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new temporary directory, deleted after the virtual machine terminates.
   * Temporary directory is created relative to a globally picked temporary directory
   * for the lifetime span of this class.
   * 
   * @see #globalTempDir()
   */
  public static File newTempDir() {
    // TODO: implement and test.
    throw new UnsupportedOperationException("not implemented");
  }

  /**
   * Creates a new temporary file. The file is physically created on disk, but is not 
   * opened.
   */
  public static File newTempFile() {
    // TODO: implement and test.
    throw new UnsupportedOperationException("not implemented");
  }

  //
  // Characters and strings. Delegates to RandomStrings.
  //

  /** @see RandomStrings#randomAsciiString(Random) */
  public static String randomAsciiString() { 
    return RandomStrings.randomAsciiString(getRandom()); 
  }

  /** @see RandomStrings#randomCharString(Random, char, char, int) */
  public static String randomCharString(char min, char max, int maxLength) {
    return RandomStrings.randomCharString(getRandom(), min, max, maxLength);
  }

  /** @see RandomStrings#randomUnicodeString(Random) */
  public static String randomUnicodeString() {
    return RandomStrings.randomUnicodeString(getRandom());
  }

  /** @see RandomStrings#randomUnicodeString(Random, int) */
  public static String randomUnicodeString(Random r, int maxLength) {
    return RandomStrings.randomUnicodeString(getRandom(), maxLength);
  }

  /** @see RandomStrings#randomUnicodeStringOfLength(Random, int) */
  public static String randomUnicodeStringOfLength(int maxLength) {
    return RandomStrings.randomUnicodeStringOfLength(getRandom(), maxLength);
  }

  /** @see RandomStrings#randomUnicodeStringOfUTF8Length(Random, int) */
  public static String randomUnicodeStringOfUTF8Length(int length) {
    return RandomStrings.randomUnicodeStringOfUTF8Length(getRandom(), length);
  }

  /** @see RandomStrings#randomRealisticUnicodeString(Random) */
  public static String randomRealisticUnicodeString() {
    return RandomStrings.randomRealisticUnicodeString(getRandom());
  }

  /** @see RandomStrings#randomRealisticUnicodeString(Random, int) */
  public static String randomRealisticUnicodeString(int maxLength) {
    return RandomStrings.randomRealisticUnicodeString(getRandom(), maxLength);
  }

  /** @see RandomStrings#randomRealisticUnicodeString(Random, int, int) */
  public static String randomRealisticUnicodeString(int minLength, int maxLength) {
    return RandomStrings.randomRealisticUnicodeString(getRandom(), minLength, maxLength);
  }
}
