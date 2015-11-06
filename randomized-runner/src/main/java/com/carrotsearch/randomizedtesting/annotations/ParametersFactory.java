package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.*;
import java.util.Formatter;

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
   * Use default argument formatting for test cases.
   */
  public static final String DEFAULT_FORMATTING = "default";
  
  /**
   * Specify custom formatting for test names (constructor arguments).
   * The string must be a valid argument to Java's built-in {@link Formatter}.
   * Constructor arguments are available in the order they were returned 
   * from {@link ParametersFactory}. Not all arguments have to be used,
   * for example: {@code foo=%1$s} would select only the second argument (indexes
   * are zero-based).
   */
  String argumentFormatting() default DEFAULT_FORMATTING;

  /**
   * Shuffles the order of tests generated for the parameter set.
   */
  boolean shuffle() default true;
}
