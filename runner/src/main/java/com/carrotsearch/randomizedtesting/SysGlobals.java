package com.carrotsearch.randomizedtesting;

import org.junit.runners.JUnit4;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seeds;

/**
 * Global names for system properties controlling the behavior of {@link JUnit4} ANT task
 * and {@link RandomizedRunner}.
 */
public final class SysGlobals {
  // No instances.
  private SysGlobals() {}
  
  /**
   * Statically initialized global prefix for all system properties. This can be initialized
   * only once. 
   */
  private static final String GLOBAL_PREFIX;

  /**
   * Default prefix for all properties.
   */
  private static final String DEFAULT_PREFIX = "rt";

  /**
   * A common prefix for all system properties used by <code>randomizedtesting</code>
   * packages. It is discouraged to change this property but it may be used to resolve
   * conflicts with packages that have overlapping property names.
   */
  public static final String SYSPROP_PREFIX = DEFAULT_PREFIX + ".prefix";

  /**
   * Static initializer for {@link #GLOBAL_PREFIX}.
   */
  static {
    String globalPrefixOverride = System.getProperty(SYSPROP_PREFIX);
    if (globalPrefixOverride == null) {
      globalPrefixOverride = DEFAULT_PREFIX;
    }
    GLOBAL_PREFIX = globalPrefixOverride.trim();
  }

  /**
   * Enable or disable stack filtering. 
   */
  public static final String SYSPROP_STACKFILTERING = prefixProperty("stackfiltering");

  /**
   * System property with an integer defining global initialization seeds for all
   * random generators. Should guarantee test reproducibility.
   */
  public static final String SYSPROP_RANDOM_SEED = prefixProperty("seed");

  /**
   * The global override for the number of each test's repetitions.
   */
  public static final String SYSPROP_ITERATIONS = prefixProperty("iters");

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
  public static final String SYSPROP_TESTCLASS = prefixProperty("class");

  /**
   * Global override for picking out a single test method to execute. If a
   * matching method exists in more than one class, it will be executed. 
   */
  public static final String SYSPROP_TESTMETHOD = prefixProperty("method");

  /**
   * If there's a runaway thread, how many times do we try to interrupt and
   * then kill it before we give up? Runaway threads may affect other tests (bad idea).
   *  
   * @see #SYSPROP_KILLWAIT
   */
  public static final String SYSPROP_KILLATTEMPTS = prefixProperty("killattempts");

  /**
   * If there's a runaway thread, how long should we wait between iterations of 
   * putting a silver bullet through its heart?
   * 
   * @see #SYSPROP_KILLATTEMPTS
   */
  public static final String SYSPROP_KILLWAIT = prefixProperty("killwait");

  /**
   * Global override for a single test case's maximum execution time after which
   * it is considered out of control and an attempt to interrupt it is executed.
   * Timeout in millis. 
   */
  public static final String SYSPROP_TIMEOUT = prefixProperty("timeout");

  /**
   * If <code>true</code>, append seed parameter to all methods. Methods that are for some
   * reason repeated (due to {@link Repeat} annotation or multiple {@link Seeds}, for example)
   * are always postfixed with the seed to discriminate tests from each other. Otherwise many
   * GUI clients have a problem in telling which test result was which.
   */
  public static final String SYSPROP_APPEND_SEED = prefixProperty("appendseed");
  
  /**
   * Prefix a given property name with a common prefix. The prefix itself can be overriden
   * using {@link #SYSPROP_PREFIX}.
   */
  public static String prefixProperty(String propertyName) {
    if (GLOBAL_PREFIX.isEmpty()) {
      return propertyName;
    } else {
      return GLOBAL_PREFIX + "." + propertyName;
    }
  }  
}
