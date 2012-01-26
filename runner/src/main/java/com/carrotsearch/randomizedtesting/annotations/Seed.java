package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.carrotsearch.randomizedtesting.SysGlobals;

/**
 * Defines the starting seed for a given test or the entire suite. 
 * 
 * <p>If applied to the
 * suite, it semantically overrides {@link SysGlobals#SYSPROP_RANDOM_SEED}, but 
 * does not affect individual test cases (these should be repeatable anyway).</p>
 * 
 * <p>If applied to the method, it overrides the default randomized value that is derived
 * from the global suite's seed.</p>
 * 
 * <p>Typically, you'll want to override the class's seed to make the test repeat a "fixed"
 * scenario. Occasionally if there's a single failing test case for repeated tests, one
 * may want to override both to fix both the class's randomness and a given test case randomness.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface Seed {
  /**
   * The seed expressed as a hexadecimal long number or a string <code>random</code> to 
   * indicate randomized seed should be used (default value).
   * 
   * <p>The default value <code>random</code> can be used to construct a list of known
   * seeds for which a test previously failed and a random seed in addition to that (coverage
   * of previous failures + randomized run). See {@link Seeds} for more info.
   */
  String value() default "random";
}