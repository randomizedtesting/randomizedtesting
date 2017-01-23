package com.carrotsearch.randomizedtesting;

import org.junit.runners.JUnit4;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seeds;
import com.carrotsearch.randomizedtesting.rules.RequireAssertionsRule;

/**
 * Global names for system properties controlling the behavior of {@link JUnit4} ANT task
 * and {@link RandomizedRunner}.
 */
public final class SysGlobals {
  /** System property passed to forked VMs: VM ID (sequential integer between 0 and the (number of concurrent jvms - 1)). */
  public static final String CHILDVM_SYSPROP_JVM_ID = "junit4.childvm.id";
  
  /** System property passed to forked VMs: the number of concurrent JVMs. */
  public static final String CHILDVM_SYSPROP_JVM_COUNT = "junit4.childvm.count";

  private final static Object lock = new Object();

  /**
   * Default prefix for all properties.
   */
  private static final String DEFAULT_PREFIX = "tests";

  /**
   * A common prefix for all system properties used by <code>randomizedtesting</code>
   * packages. It is discouraged to change this property but it may be used to resolve
   * conflicts with packages that have overlapping property names.
   */
  private static final String SYSPROP_PREFIX = DEFAULT_PREFIX + ".prefix";

  /**
   * Global singleton. Initialized once.
   */
  private static SysGlobals singleton;

  /**
   * Singleton initialization stack for easier debugging.
   */
  private static StackTraceElement[] singletonInitStack;

  /** Initialized singleton's prefix. */
  private final String prefix;
  
  /* Property names, rendered. */
  private final String SYSPROP_STACKFILTERING; 
  private final String SYSPROP_RANDOM_SEED;
  private final String SYSPROP_ITERATIONS;
  private final String SYSPROP_TESTCLASS;
  private final String SYSPROP_TESTMETHOD;
  private final String SYSPROP_TESTFILTER;
  private final String SYSPROP_KILLATTEMPTS;
  private final String SYSPROP_KILLWAIT;
  private final String SYSPROP_TIMEOUT;
  private final String SYSPROP_TIMEOUT_SUITE;
  private final String SYSPROP_APPEND_SEED;
  private final String SYSPROP_ASSERTS;

  // Singleton constructor.
  private SysGlobals(String prefix) {
    this.prefix = prefix;

    this.SYSPROP_STACKFILTERING = prefixWith(prefix, "stackfiltering");
    this.SYSPROP_RANDOM_SEED    = prefixWith(prefix, "seed");
    this.SYSPROP_ITERATIONS     = prefixWith(prefix, "iters");
    this.SYSPROP_TESTCLASS      = prefixWith(prefix, "class");         
    this.SYSPROP_TESTMETHOD     = prefixWith(prefix, "method");
    this.SYSPROP_TESTFILTER     = prefixWith(prefix, "filter");
    this.SYSPROP_KILLATTEMPTS   = prefixWith(prefix, "killattempts");
    this.SYSPROP_KILLWAIT       = prefixWith(prefix, "killwait");
    this.SYSPROP_TIMEOUT        = prefixWith(prefix, "timeout");
    this.SYSPROP_TIMEOUT_SUITE  = prefixWith(prefix, "timeoutSuite");
    this.SYSPROP_APPEND_SEED    = prefixWith(prefix, "appendseed");
    this.SYSPROP_ASSERTS        = prefixWith(prefix, "asserts");
  }

  /** */
  private String prefixWith(String prefix, String propertyName) {
    if (prefix.isEmpty()) {
      return propertyName;
    } else {
      return prefix + (prefix.endsWith(".") ? "" : ".") + propertyName;
    }
  }

  /** */
  private static SysGlobals singleton() {
    synchronized (lock) {
      if (singleton == null) {
        String prefix = System.getProperty(SYSPROP_PREFIX);
        if (prefix == null) {
          prefix = DEFAULT_PREFIX;
        }
        initializeWith(prefix);
      }
      return singleton;
    }
  }

  /** */
  public static SysGlobals initializeWith(String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException("Prefix must not be null.");
    }

    synchronized (lock) {
      if (singleton == null) {
        singleton = new SysGlobals(prefix);
        singletonInitStack = Thread.currentThread().getStackTrace();
      }

      if (!singleton.prefix.equals(prefix)) {
        Exception e = new Exception("Original singleton initialization stack.");
        e.setStackTrace(singletonInitStack);
        throw new RuntimeException("A singleton has been initialized already with a " +
            "different prefix: existing=" + singleton.prefix + ", attempted=" + prefix, e);
      }

      return singleton;
    }
  }

  /**
   * Global system property that holds the prefix used by other properties.  
   */
  public static String SYSPROP_PREFIX() { return SYSPROP_PREFIX; }

  /**
   * Static singleton's property prefix. Initializes it if not already initialized. 
   */
  public static String CURRENT_PREFIX() { return singleton().prefix; }

  /**
   * Enable or disable stack filtering. 
   */
  public static String SYSPROP_STACKFILTERING() { return singleton().SYSPROP_STACKFILTERING; }

  /**
   * System property with an integer defining global initialization seeds for all
   * random generators. Should guarantee test reproducibility.
   */
  public static String SYSPROP_RANDOM_SEED() { return singleton().SYSPROP_RANDOM_SEED; }

  /**
   * The global override for the number of each test's repetitions.
   */
  public static String SYSPROP_ITERATIONS() { return singleton().SYSPROP_ITERATIONS; }

  /**
   * Global override for picking out a single test class to execute. All other
   * classes are ignored. The property can contain "globbing patterns" similar
   * to shell expansion patterns. For example:
   * <pre>
   * *MyTest
   * </pre>
   * will pick all classes ending in MyTest (in any package, including nested static
   * classes if they appear on input).
   */
  public static String SYSPROP_TESTCLASS() { return singleton().SYSPROP_TESTCLASS; }

  /**
   * Global override for picking out a single test method to execute. If a
   * matching method exists in more than one class, it will be executed. 
   */
  public static String SYSPROP_TESTMETHOD() { return singleton().SYSPROP_TESTMETHOD; }

  /**
   * Global test filter.
   */
  public static String SYSPROP_TESTFILTER() { return singleton().SYSPROP_TESTFILTER; }
  
  /**
   * If there's a runaway thread, how many times do we try to interrupt and
   * then kill it before we give up? Runaway threads may affect other tests (bad idea).
   */
  public static String SYSPROP_KILLATTEMPTS() { return singleton().SYSPROP_KILLATTEMPTS; }

  /**
   * If there's a runaway thread, how long should we wait between iterations of 
   * putting a silver bullet through its heart?
   */
  public static String SYSPROP_KILLWAIT() { return singleton().SYSPROP_KILLWAIT; }

  /**
   * Global override for a single test case's maximum execution time after which
   * it is considered out of control and an attempt to interrupt it is executed.
   * 
   * <p>The timeout value should be in milliseconds. If the value is trailed by a 
   * "!" then the timeout value takes precedence over annotations, otherwise annotations
   * take precedence over the default timeout. This is useful for running debugging
   * sessions, for example, when default timeouts may be too short.
   * 
   * @see RandomizedRunner#DEFAULT_TIMEOUT
   */
  public static String SYSPROP_TIMEOUT() { return singleton().SYSPROP_TIMEOUT; }

  /**
   * Global override for entire suite's maximum execution time after which
   * it is considered out of control. 
   * 
   * <p>The timeout value should be in milliseconds. If the value is trailed by a 
   * "!" then the timeout value takes precedence over annotations, otherwise annotations
   * take precedence over the default timeout. This is useful for running debugging
   * sessions, for example, when default timeouts may be too short.
   * 
   * @see RandomizedRunner#DEFAULT_TIMEOUT_SUITE
   */
  public static String SYSPROP_TIMEOUT_SUITE() { return singleton().SYSPROP_TIMEOUT_SUITE; }

  /**
   * If <code>true</code>, append seed parameter to all methods. Methods that are for some
   * reason repeated (due to {@link Repeat} annotation or multiple {@link Seeds}, for example)
   * are always postfixed with the seed to discriminate tests from each other. Otherwise many
   * GUI clients have a problem in telling which test result was which.
   */
  public static String SYSPROP_APPEND_SEED() { return singleton().SYSPROP_APPEND_SEED; } 

  /**
   * Returns the property name to express the desired status of assertions during tests.
   * 
   * @see RequireAssertionsRule
   */
  public static String SYSPROP_ASSERTS() { return singleton().SYSPROP_ASSERTS; } 

  /**
   * Prefix a given property name with a common prefix. The prefix itself can be overridden
   * using <code>SYSPROP_PREFIX</code>. This method initializes static singleton property
   * names so it shouldn't be called on class initialization anywhere.
   */
  public static String prefixProperty(String propertyName) {
    return singleton().prefixWith(singleton.prefix, propertyName);
  }
}
