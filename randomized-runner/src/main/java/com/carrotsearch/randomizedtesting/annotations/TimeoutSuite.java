package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.SysGlobals;

/**
 * Maximum execution time for an entire suite (including all hooks and tests).
 * Suite is defined as any class-scope {@link TestRule}s, {@link BeforeClass}
 * and {@link AfterClass} hooks, suite class's constructor, instance-scope
 * {@link TestRule}s, {@link Before} and {@link After} hooks and {@link Test}
 * methods.
 * 
 * <p>
 * The suite class's static initializer is <b>not</b> part of the measured code
 * (if you have static initializers in your tests, get rid of them).
 * 
 * <p>
 * Overrides the global default {@link RandomizedRunner#DEFAULT_TIMEOUT} or a
 * system property override {@link SysGlobals#SYSPROP_TIMEOUT}.
 * 
 * @see Timeout
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TimeoutSuite {
  /**
   * Timeout time in millis. The timeout time is approximate, it may take longer
   * to actually abort the suite.
   */
  public int millis();
}
