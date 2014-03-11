package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.*;

/**
 * Used to annotate methods providing parameters for parameterized tests. The method
 * annotated as the factory must be static, public, parameterless and must have a return
 * type assignable to {@link Iterable}<code>&lt;Object[]&gt;</code>.
 * 
 * <p>The iterable must return arrays conforming to the suite class's constructor
 * with respect to the number and types of parameters.
 * 
 * <p>The constructor's parameters can be annotated with {@link Name} to provide
 * more descriptive parameter names for test descriptions.
 * 
 * @see Name
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ParametersFactory {
  /**
   * Shuffles the order of tests generated for the 
   * parameter set.
   */
  boolean shuffle() default true;
}
