package com.carrotsearch.randomizedtesting;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.Listeners;
import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.Validators;
import com.carrotsearch.randomizedtesting.generators.RandomInts;
import com.carrotsearch.randomizedtesting.generators.RandomPicks;
import com.carrotsearch.randomizedtesting.generators.RandomStrings;

/**
 * Common scaffolding for subclassing randomized tests.
 * 
 * @see Validators
 * @see Listeners
 * @see RandomizedContext
 */
@RunWith(RandomizedRunner.class)
public class RandomizedTest extends Assert {
  /**
   * The global multiplier property (Double).
   * 
   * @see #multiplier()
   */
  public static final String SYSPROP_MULTIPLIER = "randomized.multiplier";

  /* Commonly used charsets (these must be supported by every JVM). */

  protected static final Charset UTF8 = Charset.forName("UTF-8");
  protected static final Charset UTF16 = Charset.forName("UTF-16");
  protected static final Charset ISO8859_1 = Charset.forName("ISO-8859-1");
  protected static final Charset US_ASCII = Charset.forName("US-ASCII");

  /* This charset does not need to be supported, but I don't know any JVM under which it wouldn't be. */
  
  protected static final Charset UTF32 = Charset.forName("UTF-32");

  /** 
   * Default multiplier.
   *  
   * @see #SYSPROP_MULTIPLIER
   */
  private static final double DEFAULT_MULTIPLIER = 1.0d;

  /**
   * Shortcut for {@link RandomizedContext#current()}. 
   */
  protected static RandomizedContext getContext() {
    return RandomizedContext.current();
  }

  /**
   * Returns true if we're running nightly tests.
   * @see Nightly
   */
  protected static boolean isNightly() {
    return getContext().isNightly();
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

  /** @see Random#nextGaussian() */
  protected static double  randomGaussian() { return getRandom().nextGaussian(); }

  //
  // Delegates to RandomInts.
  //

  /** 
   * A random integer from 0..max (inclusive). 
   */
  protected static int randomInt(int max) { 
    return RandomInts.randomInt(getRandom(), max); 
  }

  /** 
   * A random integer from <code>min</code> to <code>max</code> (inclusive). 
   */
  protected static int randomIntBetween(int min, int max) {
    return RandomInts.randomIntBetween(getRandom(), min, max);
  }

  //
  // Delegates to RandomPicks
  //
  
  /**
   * Pick a random object from the given array. The array must not be empty.
   */
  protected static <T> T randomFrom(T [] array) {
    return RandomPicks.randomFrom(getRandom(), array);
  }

  /**
   * Pick a random object from the given list.
   */
  protected static <T> T randomFrom(List<T> list) {
    return RandomPicks.randomFrom(getRandom(), list);
  }

  //
  // "multiplied" or scaled value pickers. These will be affected by global multiplier.
  //

  /**
   * A multiplier can be used to linearly scale certain values. It can be used to make data
   * or iterations of certain tests "heavier" for nightly runs, for example.
   *
   * @see #SYSPROP_MULTIPLIER
   */
  public static double multiplier() {
    checkContext();
    return systemPropertyAsDouble(SYSPROP_MULTIPLIER, DEFAULT_MULTIPLIER);
  }

  // Methods to help with I/O and environment.  

  /**
   * @see #globalTempDir()
   */
  private static File globalTempDir;
  
  /**
   * Subfolders under {@link #globalTempDir} are created synchronously, so we don't need
   * to mangle filenames.
   */
  private static int  tempSubFileNameCount;

  /**
   * Global temporary directory created for the duration of this class's lifespan. If
   * multiple class loaders are used, there may be more global temp dirs, but it
   * shouldn't really be the case in practice.
   */
  protected static File globalTempDir() {
    checkContext();
    synchronized (RandomizedTest.class) {
      if (globalTempDir == null) {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        if (tempDirPath == null) 
          throw new Error("No property java.io.tmpdir?");

        File tempDir = new File(tempDirPath);
        if (!tempDir.isDirectory() || !tempDir.canWrite()) {
          throw new Error("Temporary folder not accessible: "
              + tempDir.getAbsolutePath());
        }

        SimpleDateFormat tsFormat = new SimpleDateFormat("'tests-'yyyyMMddHHmmss'-'SSS");
        int retries = 10;
        do {
          String dirName = tsFormat.format(new Date());
          final File tmpFolder = new File(tempDir, dirName);
          // I assume mkdir is filesystem-atomic and only succeeds if the 
          // directory didn't previously exist?
          if (tmpFolder.mkdir()) {
            globalTempDir = tmpFolder;
            Runtime.getRuntime().addShutdownHook(new Thread() {
              public void run() {
                try {
                  // We need canonical path on the root dir because it may
                  // be symlinked initially (macos).
                  forceDeleteRecursively(globalTempDir.getCanonicalFile());
                } catch (IOException e) {
                  // Not much else to do but to log and quit.
                  System.err.println("Error while deleting temporary folder '" +
                      globalTempDir.getAbsolutePath() +
                  		"': " + e.getMessage());
                }

                if (globalTempDir.exists()) {
                  System.err.println("Could not delete temporary folder entirely: "
                      + globalTempDir.getAbsolutePath());
                }                
              }
            });
            return globalTempDir;
          }
        } while (retries-- > 0);
        throw new RuntimeException("Could not create temporary space in: "
            + tempDir);
      }
      return globalTempDir;
    }
  }

  /**
   * Creates a new temporary directory, deleted after the virtual machine terminates.
   * Temporary directory is created relative to a globally picked temporary directory.
   * 
   * @see #globalTempDir()
   */
  protected static File newTempDir() {
    checkContext();
    synchronized (RandomizedTest.class) {
      File tempDir = new File(globalTempDir(), nextTempName());
      if (!tempDir.mkdir()) throw new RuntimeException("Could not create temporary folder: "
          + tempDir.getAbsolutePath());
      return tempDir;
    }
  }

  /**
   * Creates a new temporary file. The file is physically created on disk, but is not 
   * locked or opened.
   */
  protected static File newTempFile() {
    checkContext();
    synchronized (RandomizedTest.class) {
      File tempDir = new File(globalTempDir(), nextTempName());
      try {
        if (!tempDir.createNewFile()) 
          throw new RuntimeException("Could not create temporary file: " 
              + tempDir.getAbsolutePath());
      } catch (IOException e) {
        throw new RuntimeException("Could not create temporary file: " 
            + tempDir.getAbsolutePath(), e);
      }
      return tempDir;
    }
  }

  /** Next temporary filename. */
  private static String nextTempName() {
    return String.format("%04d", tempSubFileNameCount++);
  }

  /**
   * Recursively delete a folder (or file). This attempts to delete everything that
   * can be deleted, but possibly can leave things behind if files are locked for example.
   */
  protected static void forceDeleteRecursively(File fileOrDir) throws IOException {
    if (fileOrDir.isDirectory()) {
      // Not a symlink? Delete contents first.
      if (fileOrDir.getCanonicalPath().equals(fileOrDir.getAbsolutePath())) {
        for (File f : fileOrDir.listFiles()) {
          forceDeleteRecursively(f);
        }
      }
    }

    fileOrDir.delete();
  }

  /** 
   * Return a random Locale from the available locales on the system.
   * 
   * <p>Warning: This test assumes the returned array of locales is repeatable from jvm execution
   * to jvm execution. It _may_ be different from jvm to jvm and as such, it can render
   * tests execute in a different way.</p>
   */
  protected static Locale randomLocale() {
    Locale[] availableLocales = Locale.getAvailableLocales();
    Arrays.sort(availableLocales, new Comparator<Locale>() {
      public int compare(Locale o1, Locale o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });
    return randomFrom(availableLocales);
  }

  /** 
   * Return a random TimeZone from the available timezones on the system.
   * 
   * <p>Warning: This test assumes the returned array of time zones is repeatable from jvm execution
   * to jvm execution. It _may_ be different from jvm to jvm and as such, it can render
   * tests execute in a different way.</p>
   */
  protected static TimeZone randomTimeZone() {
    final String[] availableIDs = TimeZone.getAvailableIDs();
    Arrays.sort(availableIDs);
    return TimeZone.getTimeZone(randomFrom(availableIDs));
  }

  //
  // Characters and strings. Delegates to RandomStrings.
  //

  /** @see RandomStrings#randomAsciiString(Random) */
  protected static String randomAsciiString() {
    checkContext();
    return RandomStrings.randomAsciiString(getRandom()); 
  }

  /** @see RandomStrings#randomCharString(Random, char, char, int) */
  protected static String randomCharString(char min, char max, int maxLength) {
    checkContext();
    return RandomStrings.randomCharString(getRandom(), min, max, maxLength);
  }

  /** @see RandomStrings#randomUnicodeString(Random) */
  protected static String randomUnicodeString() {
    checkContext();
    return RandomStrings.randomUnicodeString(getRandom());
  }

  /** @see RandomStrings#randomUnicodeString(Random, int) */
  protected static String randomUnicodeString(int maxUtf16Length) {
    checkContext();
    return RandomStrings.randomUnicodeString(getRandom(), maxUtf16Length);
  }

  /** @see RandomStrings#randomUnicodeStringOfUTF8Length(Random, int) */
  protected static String randomUnicodeStringOfUTF8Length(int utf8Length) {
    checkContext();
    return RandomStrings.randomUnicodeStringOfUTF8Length(getRandom(), utf8Length);
  }

  /** @see RandomStrings#randomUnicodeStringOfUTF16Length(Random, int) */
  protected static String randomUnicodeStringOfUTF16Length(int utf16Length) {
    checkContext();
    return RandomStrings.randomUnicodeStringOfUTF16Length(getRandom(), utf16Length);
  }

  /** @see RandomStrings#randomRealisticUnicodeString(Random) */
  protected static String randomRealisticUnicodeString() {
    checkContext();
    return RandomStrings.randomRealisticUnicodeString(getRandom());
  }

  /** @see RandomStrings#randomRealisticUnicodeString(Random, int) */
  protected static String randomRealisticUnicodeString(int maxCodepointLength) {
    checkContext();
    return RandomStrings.randomRealisticUnicodeString(getRandom(), maxCodepointLength);
  }

  /** @see RandomStrings#randomRealisticUnicodeString(Random, int, int) */
  protected static String randomRealisticUnicodeString(int minCodepointLength, int maxCodepointLength) {
    checkContext();
    return RandomStrings.randomRealisticUnicodeString(getRandom(), minCodepointLength, maxCodepointLength);
  }

  // 
  // wrappers for utility methods elsewhere that don't require try..catch blocks
  // and rethrow the original checked exception if needed. dirty a bit, but saves
  // keystrokes...
  //
  
  /**
   * Same as {@link Thread#sleep(long)}.  
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Rethrow.rethrow(e);
    }
  }

  //
  // Extensions of Assume (with a message).
  //

  /**
   * @param condition
   *          If <code>false</code> an {@link InternalAssumptionViolatedException} is
   *          thrown by this method and the test case (should be) ignored (or
   *          rather technically, flagged as a failure not passing a certain
   *          assumption). Tests that are assumption-failures do not break
   *          builds (again: typically).
   * @param message
   *          Message to be included in the exception's string.
   */
  public static void assumeTrue(boolean condition, String message) {
    if (!condition) {
      // @see {@link Rants#RANT_2}.
      throw new InternalAssumptionViolatedException(message);
    }
  }

  /**
   * Reverse of {@link #assumeTrue(boolean, String)}.
   */
  public static void assumeFalse(boolean condition, String message) {
    assumeTrue(!condition, message);
  }

  /**
   * Assume <code>t</code> is <code>null</code>.
   */
  public static void assumeNoException(String msg, Throwable t) {
    if (t != null) {
      // This does chain the exception as the cause.
      throw new InternalAssumptionViolatedException(msg, t);
    }
  }

  //
  // System properties and their conversion to common types, with defaults.
  //
  
  /** 
   * Get a system property and convert it to a double, if defined. Otherwise, return the default value.
   */
  public static double systemPropertyAsDouble(String propertyName, double defaultValue) {
    String v = System.getProperty(propertyName);
    if (v != null && !v.trim().isEmpty()) {
      return Double.parseDouble(v.trim());
    } else {
      return defaultValue;
    }
  }

  /** 
   * Get a system property and convert it to a float, if defined. Otherwise, return the default value.
   */
  public static float systemPropertyAsFloat(String propertyName, float defaultValue) {
    String v = System.getProperty(propertyName);
    if (v != null && !v.trim().isEmpty()) {
      return Float.parseFloat(v.trim());
    } else {
      return defaultValue;
    }
  }

  /** 
   * Get a system property and convert it to an int, if defined. Otherwise, return the default value.
   */
  public static int systemPropertyAsInt(String propertyName, int defaultValue) {
    String v = System.getProperty(propertyName);
    if (v != null && !v.trim().isEmpty()) {
      return Integer.parseInt(v.trim());
    } else {
      return defaultValue;
    }
  }

  /** 
   * Get a system property and convert it to a long, if defined. Otherwise, return the default value.
   */
  public static float systemPropertyAsLong(String propertyName, int defaultValue) {
    String v = System.getProperty(propertyName);
    if (v != null && !v.trim().isEmpty()) {
      return Long.parseLong(v.trim());
    } else {
      return defaultValue;
    }
  }

  /** Boolean constants mapping. */
  @SuppressWarnings("serial")
  private final static HashMap<String, Boolean> BOOLEANS = new HashMap<String, Boolean>() {{
    put(   "true", true); put(   "false", false);
    put(     "on", true); put(     "off", false);
    put(    "yes", true); put(      "no", false);
    put("enabled", true); put("disabled", false);
  }};

  /**
   * Get a system property and convert it to a boolean, if defined. This method returns
   * <code>true</code> if the property exists an is set to any of the following strings
   * (case-insensitive): <code>true</code>, <code>on</code>, <code>yes</code>, <code>enabled</code>.
   * 
   * <p><code>false</code> is returned if the property exists and is set to any of the
   * following strings (case-insensitive):
   * <code>false</code>, <code>off</code>, <code>no</code>, <code>disabled</code>.
   */
  public static boolean systemPropertyAsBoolean(String propertyName, boolean defaultValue) {
    String v = System.getProperty(propertyName);

    if (v != null && !v.trim().isEmpty()) {
      v = v.trim();
      Boolean result = BOOLEANS.get(v);
      if (result != null) 
        return result.booleanValue();
      else
        throw new IllegalArgumentException("Boolean value expected " +
      		"(true/false, on/off, enabled/disabled, yes/no): " + v);
    } else {
      return defaultValue;
    }
  }

  //
  // Miscellaneous infrastructure.
  //

  /**
   * Ensures we're running with an initialized {@link RandomizedContext}.
   */
  private static void checkContext() {
    // Will throw an exception if not available.
    RandomizedContext.current(); 
  }
}
