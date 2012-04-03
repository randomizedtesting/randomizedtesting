package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * An annotation indicating a given test case (or suite) should run only during
 * nightly tests.
 * 
 * <p>The notion of "nightly" tests is based on an assumption that these tests can
 * take longer than usual or require more resources than usual. Nightly tests will
 * be most commonly used with higher scaling multipliers as in 
 * ({@link RandomizedTest#SYSPROP_MULTIPLIER}.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@TestGroup(enabled = false)
public @interface Nightly {
  /** Additional description, if needed. */
  String value() default "";
}
