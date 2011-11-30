package com.carrotsearch.randomizedtesting;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
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
import org.junit.Assume;
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
   * 
   * @see #scaledRandomIntBetween(int, int)
   */
  protected static int randomIntBetween(int min, int max) {
    return RandomInts.randomIntBetween(getRandom(), min, max);
  }

  /** 
   * An alias for {@link #randomIntBetween(int, int)}. 
   * 
   * @see #scaledRandomIntBetween(int, int)
   */
  protected static int between(int min, int max) {
    return randomIntBetween(min, max);
  }

  /** 
   * Returns a random value greater or equal to <code>min</code>. The value
   * picked is affected by {@link #isNightly()} and {@link #multiplier()}.
   * 
   * @see #scaledRandomIntBetween(int, int)
   */
  protected static int atLeast(int min) {
    if (min < 0) throw new IllegalArgumentException("atLeast requires non-negative argument: " + min);

    min = (int) Math.min(min, (isNightly() ? 3 * min : min) * multiplier());
    int max = (int) Math.min(Integer.MAX_VALUE, (long) min + (min / 2));
    return randomIntBetween(min, max);
  }

  /** 
   * Returns a non-negative random value smaller or equal <code>max</code>. The value
   * picked is affected by {@link #isNightly()} and {@link #multiplier()}.
   * 
   * <p>This method is effectively an alias to:
   * <pre>
   * scaledRandomIntBetween(0, max)
   * </pre>
   * 
   * @see #scaledRandomIntBetween(int, int)
   */
  protected static int atMost(int max) {
    if (max < 0) throw new IllegalArgumentException("atMost requires non-negative argument: " + max);
    return scaledRandomIntBetween(0, max);
  }

  /**
   * Rarely returns <code>true</code> in about 10% of all calls (regardless of the
   * {@link #isNightly()} mode).
   */
  protected static boolean rarely() {
    return randomInt(100) >= 90;
  }

  /**
   * The exact opposite of {@link #rarely()}.
   */
  protected static boolean frequently() {
    return !rarely();
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
   * <p>The default multiplier value is 1.</p>
   *
   * @see #SYSPROP_MULTIPLIER
   * @see #DEFAULT_MULTIPLIER
   */
  public static double multiplier() {
    checkContext();
    return systemPropertyAsDouble(SYSPROP_MULTIPLIER, DEFAULT_MULTIPLIER);
  }

  /**
   * Returns a "scaled" number of iterations for loops which can have a variable
   * iteration count. This method is effectively 
   * an alias to {@link #scaledRandomIntBetween(int, int)}.
   */
  public static int iterations(int min, int max) {
    return scaledRandomIntBetween(min, max);
  }

  /**
   * Returns a "scaled" random number between min and max (inclusive). The number of 
   * iterations will fall between [min, max], but the selection will also try to 
   * achieve the points below: 
   * <ul>
   *   <li>the multiplier can be used to move the number of iterations closer to min
   *   (if it is smaller than 1) or closer to max (if it is larger than 1). Setting
   *   the multiplier to 0 will always result in picking min.</li>
   *   <li>on normal runs, the number will be closer to min than to max.</li>
   *   <li>on nightly runs, the number will be closer to max than to min.</li>
   * </ul>
   * 
   * @param min Minimum (inclusive).
   * @param max Maximum (inclusive).
   * @return Returns a random number between min and max.
   */
  public static int scaledRandomIntBetween(int min, int max) {
    if (min < 0) throw new IllegalArgumentException("min must be >= 0: " + min);
    if (min > max) throw new IllegalArgumentException("max must be >= min: " + min + ", " + max);

    double point = Math.min(1, Math.abs(randomGaussian()) * 0.3) * multiplier();
    double range = max - min;
    int scaled = (int) Math.round(Math.min(point * range, range));
    if (isNightly()) {
      return max - scaled;
    } else {
      return min + scaled; 
    }
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
                  forceDeleteRecursively(globalTempDir);
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
   * Creates a new temporary directory for the {@link LifecycleScope#TEST} duration.
   * 
   * @see #globalTempDir()
   */
  protected File newTempDir() {
    return newTempDir(LifecycleScope.TEST);
  }

  /**
   * Creates a temporary directory, deleted after the given lifecycle phase. 
   * Temporary directory is created relative to a globally picked temporary directory.
   */
  protected static File newTempDir(LifecycleScope scope) {
    checkContext();
    synchronized (RandomizedTest.class) {
      File tempDir = new File(globalTempDir(), nextTempName());
      if (!tempDir.mkdir()) throw new RuntimeException("Could not create temporary folder: "
          + tempDir.getAbsolutePath());
      getContext().closeAtEnd(new TempPathResource(tempDir), scope);
      return tempDir;
    }
  }

  /**
   * Registers a {@link Closeable} resource that should be closed after the test
   * completes.
   * 
   * @return <code>resource</code> (for call chaining).
   */
  protected <T extends Closeable> T closeAfterTest(T resource) {
    return getContext().closeAtEnd(resource, LifecycleScope.TEST);
  }

  /**
   * Registers a {@link Closeable} resource that should be closed after the suite
   * completes.
   * 
   * @return <code>resource</code> (for call chaining).
   */
  protected static <T extends Closeable> T closeAfterSuite(T resource) {
    return getContext().closeAtEnd(resource, LifecycleScope.SUITE);
  }

  /**
   * Creates a new temporary file for the {@link LifecycleScope#TEST} duration.
   */
  protected File newTempFile() {
    return newTempFile(LifecycleScope.TEST);
  }

  /**
   * This is an absolutely hacky utility to take a vararg as input and return the array
   * of arguments as output. The name is a dollar for brevity, idea borrowed from
   * http://code.google.com/p/junitparams/.
   */
  public static Object [] $(Object... objects) {
    return objects;
  }

  /**
   * @see #$
   */
  public static Object [][] $$(Object[]... objects) {
    return objects;
  }

  /**
   * Creates a new temporary file deleted after the given lifecycle phase completes.
   * The file is physically created on disk, but is not locked or opened.
   */
  protected static File newTempFile(LifecycleScope scope) {
    checkContext();
    synchronized (RandomizedTest.class) {
      File tempFile = new File(globalTempDir(), nextTempName());
      try {
        if (!tempFile.createNewFile()) 
          throw new RuntimeException("Could not create temporary file: " 
              + tempFile.getAbsolutePath());
      } catch (IOException e) {
        throw new RuntimeException("Could not create temporary file: " 
            + tempFile.getAbsolutePath(), e);
      }
      getContext().closeAtEnd(new TempPathResource(tempFile), scope);
      return tempFile;
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
  static void forceDeleteRecursively(File fileOrDir) throws IOException {
    if (fileOrDir.isDirectory()) {
      // We are not checking for symlinks here!
      for (File f : fileOrDir.listFiles()) {
        forceDeleteRecursively(f);
      }
    }

    if (!fileOrDir.delete()) {
      RandomizedRunner.logger.warning("Could not delete: "
          + fileOrDir.getAbsolutePath());
    }
  }

  /**
   * Assign a temporary server socket. If you need a temporary port one can
   * assign a server socket and close it immediately, just to acquire its port
   * number.
   * 
   * @param scope
   *          The lifecycle scope to close the socket after. If the socket is
   *          closed earlier, nothing happens (silently dropped).
   */
  public static ServerSocket newServerSocket(LifecycleScope scope) throws IOException {
    final ServerSocket socket = new ServerSocket(0);
    getContext().closeAtEnd(new Closeable() {
      public void close() throws IOException {
        if (!socket.isClosed())
          socket.close();
      }
    }, scope);

    return socket;
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
   * Making {@link Assume#assumeTrue(boolean)} directly available.
   */
  public static void assumeTrue(boolean condition) {
    Assume.assumeTrue(condition);
  }

  /**
   * Reverse of {@link #assumeTrue(boolean)}.
   */
  public static void assumeFalse(boolean condition) {
    assumeTrue(!condition);
  }

  /**
   * Making {@link Assume#assumeNotNull(Object...)} directly available.
   */
  public static void assumeNotNull(Object... objects) {
    Assume.assumeNotNull(objects);
  }

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
  public static void assumeTrue(String message, boolean condition) {
    if (!condition) {
      // @see {@link Rants#RANT_2}.
      throw new InternalAssumptionViolatedException(message);
    }
  }

  /**
   * Reverse of {@link #assumeTrue(String, boolean)}.
   */
  public static void assumeFalse(String message, boolean condition) {
    assumeTrue(message, !condition);
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
  
  /**
   * Making {@link Assume#assumeNoException(Throwable)} directly available.
   */
  public static void assumeNoException(Throwable t) {
    Assume.assumeNoException(t);
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
      try {
        return Double.parseDouble(v.trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Double value expected for property " +
            propertyName + ": " + v, e);
      }
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
      try {
        return Float.parseFloat(v.trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Float value expected for property " +
            propertyName + ": " + v, e);
      }
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
      try {
        return Integer.parseInt(v.trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Integer value expected for property " +
            propertyName + ": " + v, e);
      }        
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
      try {
        return Long.parseLong(v.trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Long value expected for property " +
            propertyName + ": " + v, e);
      }
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
        throw new IllegalArgumentException("Boolean value expected for property " +
          propertyName + " " +
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
