package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.SysGlobals;

/**
 * Maximum execution time for a single test case. Override of a global
 * default {@link RandomizedRunner#DEFAULT_TIMEOUT} or a system property
 * override {@link SysGlobals#SYSPROP_TIMEOUT}. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface Timeout {
  /**
   * Timeout time in millis. The timeout time is approximate, it may take longer
   * to actually abort the test case. 
   */
  public int millis();
}
