package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Repeats randomized test case a given number of times. Repetitions can
 * start with a different seed (predictably derived from the first one) or 
 * start from the same seed every time (useful to check if a given test is truly
 * predictable for a given seed or not).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface Repeat {
  /**
   * Repeat this many iterations. Must be greater or equal 1.
   */
  int iterations() default 1;

  /**
   * Re-run all iterations with a constant seed. This may be helpful in checking
   * if a given test is really predictably random.
   */
  boolean useConstantSeed() default false;
}