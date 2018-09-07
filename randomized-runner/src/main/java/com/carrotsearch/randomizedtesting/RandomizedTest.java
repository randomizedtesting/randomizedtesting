package com.carrotsearch.randomizedtesting;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.Listeners;
import com.carrotsearch.randomizedtesting.annotations.Nightly;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import com.carrotsearch.randomizedtesting.generators.BiasedNumbers;
import com.carrotsearch.randomizedtesting.generators.RandomBytes;
import com.carrotsearch.randomizedtesting.generators.RandomNumbers;
import com.carrotsearch.randomizedtesting.generators.RandomPicks;
import com.carrotsearch.randomizedtesting.generators.RandomStrings;

/**
 * Common scaffolding for subclassing randomized tests.
 * 
 * @see Listeners
 * @see RandomizedContext
 */
@RunWith(RandomizedRunner.class)
public class RandomizedTest {
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
  public static RandomizedContext getContext() {
    return RandomizedContext.current();
  }

  /**
   * Returns true if {@link Nightly} test group is enabled.
   * 
   * @see Nightly
   */
  public static boolean isNightly() {
    return getContext().isNightly();
  }

  /**
   * Shortcut for {@link RandomizedContext#getRandom()}. Even though this method
   * is static, it returns per-thread {@link Random} instance, so no race conditions
   * can occur.
   * 
   * <p>It is recommended that specific methods are used to pick random values.
   */
  public static Random getRandom() {
    return getContext().getRandom();
  }

  //
  // Random value pickers. Shortcuts to methods in {@link #getRandom()} mostly.
  //

  public static boolean randomBoolean()  { return getRandom().nextBoolean();  }
  public static byte    randomByte()     { return (byte) getRandom().nextInt(); }
  public static short   randomShort()    { return (short) getRandom().nextInt(); }
  public static int     randomInt()      { return getRandom().nextInt(); }
  public static float   randomFloat()    { return getRandom().nextFloat(); }
  public static double  randomDouble()   { return getRandom().nextDouble(); }
  public static long    randomLong()     { return getRandom().nextLong(); }

  /** @see Random#nextGaussian() */
  public static double  randomGaussian() { return getRandom().nextGaussian(); }

  //
  // Biased value pickers. 
  //
  
  /**
   * A biased "evil" random float between min and max (inclusive).
   * 
   * @see BiasedNumbers#randomFloatBetween(Random, float, float)
   */
  public static float  biasedFloatBetween(float min, float max) { return BiasedNumbers.randomFloatBetween(getRandom(), min, max); }

  /**
   * A biased "evil" random double between min and max (inclusive).
   * 
   * @see BiasedNumbers#randomDoubleBetween(Random, double, double)
   */
  public static double biasedDoubleBetween(double min, double max) { return BiasedNumbers.randomDoubleBetween(getRandom(), min, max); }

  //
  // Delegates to RandomBytes.
  //

  /** 
   * Returns a byte array with random content.
   * 
   * @param length The length of the byte array. Can be zero.
   * @return Returns a byte array with random content. 
   */
  public static byte[] randomBytesOfLength(int length) { 
    return RandomBytes.randomBytesOfLength(new Random(getRandom().nextLong()), length); 
  }  

  /** 
   * Returns a byte array with random content.
   * 
   * @param minLength The minimum length of the byte array. Can be zero.
   * @param maxLength The maximum length of the byte array. Can be zero.
   * @return Returns a byte array with random content. 
   */
  public static byte[] randomBytesOfLength(int minLength, int maxLength) { 
    return RandomBytes.randomBytesOfLengthBetween(new Random(getRandom().nextLong()), minLength, maxLength); 
  }  

  //
  // Delegates to RandomNumbers.
  //

  /** 
   * A random integer from 0..max (inclusive). 
   */
  @Deprecated
  public static int randomInt(int max) {
    return RandomNumbers.randomIntBetween(getRandom(), 0, max);
  }

  /** 
   * A random long from 0..max (inclusive). 
   */
  @Deprecated
  public static long randomLong(long max) {
    return RandomNumbers.randomLongBetween(getRandom(), 0, max);
  }

  /** 
   * A random integer from <code>min</code> to <code>max</code> (inclusive).
   * 
   * @see #scaledRandomIntBetween(int, int)
   */
  public static int randomIntBetween(int min, int max) {
    return RandomNumbers.randomIntBetween(getRandom(), min, max);
  }

  /** 
   * An alias for {@link #randomIntBetween(int, int)}. 
   * 
   * @see #scaledRandomIntBetween(int, int)
   */
  public static int between(int min, int max) {
    return randomIntBetween(min, max);
  }

  /** 
   * A random long from <code>min</code> to <code>max</code> (inclusive).
   */
  public static long randomLongBetween(long min, long max) {
    return RandomNumbers.randomLongBetween(getRandom(), min, max);
  }

  /** 
   * An alias for {@link #randomLongBetween}. 
   */
  public static long between(long min, long max) {
    return randomLongBetween(min, max);
  }

  /** 
   * Returns a random value greater or equal to <code>min</code>. The value
   * picked is affected by {@link #isNightly()} and {@link #multiplier()}.
   * 
   * @see #scaledRandomIntBetween(int, int)
   */
  public static int atLeast(int min) {
    if (min < 0) throw new IllegalArgumentException("atLeast requires non-negative argument: " + min);
    return scaledRandomIntBetween(min, Integer.MAX_VALUE);
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
  public static int atMost(int max) {
    if (max < 0) throw new IllegalArgumentException("atMost requires non-negative argument: " + max);
    return scaledRandomIntBetween(0, max);
  }

  /**
   * Rarely returns <code>true</code> in about 10% of all calls (regardless of the
   * {@link #isNightly()} mode).
   */
  public static boolean rarely() {
    return randomInt(100) >= 90;
  }

  /**
   * The exact opposite of {@link #rarely()}.
   */
  public static boolean frequently() {
    return !rarely();
  }

  //
  // Delegates to RandomPicks
  //
  
  /**
   * Pick a random object from the given array. The array must not be empty.
   */
  public static <T> T randomFrom(T [] array) {
    return RandomPicks.randomFrom(getRandom(), array);
  }

  /**
   * Pick a random object from the given list.
   */
  public static <T> T randomFrom(List<T> list) {
    return RandomPicks.randomFrom(getRandom(), list);
  }

  public static byte randomFrom(byte [] array)     { return RandomPicks.randomFrom(getRandom(), array); }
  public static short randomFrom(short [] array)   { return RandomPicks.randomFrom(getRandom(), array); }
  public static int randomFrom(int [] array)       { return RandomPicks.randomFrom(getRandom(), array); }
  public static char randomFrom(char [] array)     { return RandomPicks.randomFrom(getRandom(), array); }
  public static float randomFrom(float [] array)   { return RandomPicks.randomFrom(getRandom(), array); }
  public static long randomFrom(long [] array)     { return RandomPicks.randomFrom(getRandom(), array); }
  public static double randomFrom(double [] array) { return RandomPicks.randomFrom(getRandom(), array); }

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
   * @see #multiplier()
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
  private static Path globalTempDir;
  
  /** */
  private static AtomicInteger tempSubFileNameCount = new AtomicInteger(0);

  /**
   * Global temporary directory created for the duration of this class's lifespan. If
   * multiple class loaders are used, there may be more global temp dirs, but it
   * shouldn't really be the case in practice.
   */
  public static Path globalTempDir() throws IOException {
    checkContext();
    synchronized (RandomizedTest.class) {
      if (globalTempDir == null) {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        if (tempDirPath == null) 
          throw new Error("No property java.io.tmpdir?");

        Path tempDir = Paths.get(tempDirPath);
        if (!Files.isDirectory(tempDir) || !Files.isWritable(tempDir)) {
          throw new Error("Temporary folder not accessible: " + tempDir.toAbsolutePath());
        }

        SimpleDateFormat tsFormat = new SimpleDateFormat("'tests-'yyyyMMddHHmmss'-'SSS", Locale.ROOT);
        String dirName = tsFormat.format(new Date());
        final Path tmpFolder = tempDir.resolve(dirName);
        // I assume mkdir is filesystem-atomic and only succeeds if the 
        // directory didn't previously exist?
        Files.createDirectories(tmpFolder);
        globalTempDir = tmpFolder;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @SuppressForbidden("Legitimate use of syserr.")
            public void run() {
              try {
                rmDir(globalTempDir);
              } catch (IOException e) {
                // Not much else to do but to log and quit.
                System.err.println("Could not delete temporary folder: " 
                    + globalTempDir.toAbsolutePath() + ". Cause: ");
                e.printStackTrace(System.err);
              }
            }
          });
      }
      return globalTempDir;
    }
  }

  /**
   * Creates a new temporary directory for the {@link LifecycleScope#TEST} duration.
   * 
   * @see #globalTempDir()
   */
  public Path newTempDir() throws IOException {
    return newTempDir(LifecycleScope.TEST);
  }

  /**
   * Creates a temporary directory, deleted after the given lifecycle phase. 
   * Temporary directory is created relative to a globally picked temporary directory.
   */
  public static Path newTempDir(LifecycleScope scope) throws IOException {
    checkContext();
    synchronized (RandomizedTest.class) {
      Path tempDir = globalTempDir().resolve(nextTempName());
      Files.createDirectories(tempDir);
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
  public <T extends Closeable> T closeAfterTest(T resource) {
    return getContext().closeAtEnd(resource, LifecycleScope.TEST);
  }

  /**
   * Registers a {@link Closeable} resource that should be closed after the suite
   * completes.
   * 
   * @return <code>resource</code> (for call chaining).
   */
  public static <T extends Closeable> T closeAfterSuite(T resource) {
    return getContext().closeAtEnd(resource, LifecycleScope.SUITE);
  }

  /**
   * Creates a new temporary file for the {@link LifecycleScope#TEST} duration.
   */
  public Path newTempFile() throws IOException {
    return newTempFile(LifecycleScope.TEST);
  }

  /**
   * Creates a new temporary file deleted after the given lifecycle phase completes.
   * The file is physically created on disk, but is not locked or opened.
   */
  public static Path newTempFile(LifecycleScope scope) throws IOException {
    checkContext();
    synchronized (RandomizedTest.class) {
      Path tempFile = globalTempDir().resolve(nextTempName());
      Files.createFile(tempFile);
      getContext().closeAtEnd(new TempPathResource(tempFile), scope);
      return tempFile;
    }
  }

  /** Next temporary filename. */
  protected static String nextTempName() {
    return String.format(Locale.ROOT, "%04d has-space", tempSubFileNameCount.getAndIncrement());
  }

  /**
   * Recursively delete a folder. Throws an exception if any failure occurs.
   * 
   * @param path Path to the folder to be (recursively) deleted. The folder must
   *             exist.
   */
  public static void rmDir(Path path) throws IOException {
    if (!Files.isDirectory(path)) {
      throw new IOException("Not a folder: " + path);
    }

    try {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException iterationError) throws IOException {
          if (iterationError != null) {
            throw iterationError;
          }
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
  
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }
  
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
          throw e;
        }
      });
    } catch (IOException e) {
      throw new IOException("Could not remove directory: " + path, e);
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
  public static Locale randomLocale() {
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
  public static TimeZone randomTimeZone() {
    final String[] availableIDs = TimeZone.getAvailableIDs();
    Arrays.sort(availableIDs);
    return TimeZone.getTimeZone(randomFrom(availableIDs));
  }

  //
  // Characters and strings. Delegates to RandomStrings and that in turn to StringGenerators.
  //

  /** 
   * @deprecated Use {@link #randomAsciiLettersOfLengthBetween} instead.  
   */
  @Deprecated
  public static String randomAsciiOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
    return randomAsciiLettersOfLengthBetween(minCodeUnits, maxCodeUnits);
  }

  /** 
   * @deprecated Use {@link #randomAsciiLettersOfLength} instead.  
   */
  @Deprecated
  public static String randomAsciiOfLength(int codeUnits) {
    return randomAsciiLettersOfLength(codeUnits);
  }

  /**
   * @see RandomStrings#randomAsciiLettersOfLengthBetween
   */
  public static String randomAsciiLettersOfLengthBetween(int minLetters, int maxLetters) {
    return RandomStrings.randomAsciiLettersOfLengthBetween(getRandom(), minLetters, maxLetters);
  }

  /**
   * @see RandomStrings#randomAsciiLettersOfLength
   */
  public static String randomAsciiLettersOfLength(int codeUnits) {
    return RandomStrings.randomAsciiLettersOfLength(getRandom(), codeUnits);
  }

  /**
   * @see RandomStrings#randomAsciiAlphanumOfLengthBetween
   */
  public static String randomAsciiAlphanumOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
    return RandomStrings.randomAsciiAlphanumOfLengthBetween(getRandom(), minCodeUnits, maxCodeUnits);
  }

  /**
   * @see RandomStrings#randomAsciiAlphanumOfLength
   */
  public static String randomAsciiAlphanumOfLength(int codeUnits) {
    return RandomStrings.randomAsciiAlphanumOfLength(getRandom(), codeUnits);
  }

  /**
   * @see RandomStrings#randomUnicodeOfLengthBetween
   */
  public static String randomUnicodeOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
    return RandomStrings.randomUnicodeOfLengthBetween(getRandom(),
        minCodeUnits, maxCodeUnits);
  }

  /**
   * @see RandomStrings#randomUnicodeOfLength
   */
  public static String randomUnicodeOfLength(int codeUnits) {
    return RandomStrings.randomUnicodeOfLength(getRandom(), codeUnits);
  }
  
  /**
   * @see RandomStrings#randomUnicodeOfCodepointLengthBetween
   */
  public static String randomUnicodeOfCodepointLengthBetween(int minCodePoints, int maxCodePoints) {
    return RandomStrings.randomUnicodeOfCodepointLengthBetween(getRandom(),
        minCodePoints, maxCodePoints);
  }
  
  /**
   * @see RandomStrings#randomUnicodeOfCodepointLength
   */
  public static String randomUnicodeOfCodepointLength(int codePoints) {
    return RandomStrings.randomUnicodeOfCodepointLength(getRandom(), codePoints);
  }
  
  /**
   * @see RandomStrings#randomRealisticUnicodeOfLengthBetween
   */
  public static String randomRealisticUnicodeOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
    return RandomStrings.randomRealisticUnicodeOfLengthBetween(getRandom(),
        minCodeUnits, maxCodeUnits);
  }
  
  /**
   * @see RandomStrings#randomRealisticUnicodeOfLength
   */
  public static String randomRealisticUnicodeOfLength(int codeUnits) {
    return RandomStrings.randomRealisticUnicodeOfLength(getRandom(), codeUnits);
  }
  
  /**
   * @see RandomStrings#randomRealisticUnicodeOfCodepointLengthBetween
   */
  public static String randomRealisticUnicodeOfCodepointLengthBetween(
      int minCodePoints, int maxCodePoints) {
    return RandomStrings.randomRealisticUnicodeOfCodepointLengthBetween(
        getRandom(), minCodePoints, maxCodePoints);
  }
  
  /**
   * @see RandomStrings#randomRealisticUnicodeOfCodepointLength
   */
  public static String randomRealisticUnicodeOfCodepointLength(int codePoints) {
    return RandomStrings.randomRealisticUnicodeOfCodepointLength(getRandom(),
        codePoints);
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
   *          If <code>false</code> an {@link AssumptionViolatedException} is
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
      throw new AssumptionViolatedException(message);
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
      throw new AssumptionViolatedException(msg, t);
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
