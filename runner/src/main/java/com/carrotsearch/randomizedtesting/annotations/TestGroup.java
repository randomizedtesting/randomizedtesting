package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.*;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/**
 * A test group applied to an annotation indicates that a given annotation
 * can be used on individual tests as "labels". The meaning of these labels is
 * mostly application-specific (example: {@link Nightly} which indicates slower, 
 * more intensive tests that are skipped during regular runs). 
 * 
 * <p>{@link RandomizedRunner} collects groups from all tests in a suite. A group
 * can be enabled or disabled using boolean system properties (or test 
 * hooks in the code). A test case is executed if it has no groups or if all of its groups
 * are enabled.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Inherited
public @interface TestGroup {
  /** 
   * The name of a test group. If not defined, the default (lowercased annotation
   * name) is used. 
   */
  String name() default "";

  /**
   * System property used to enable/ disable a group. If empty, a default is used:
   * <pre>
   * tests.<i>name</i>
   * </pre>
   */
  String sysProperty() default "";

  /**
   * Is the group enabled or disabled by default? 
   */
  boolean enabled() default true;
}
