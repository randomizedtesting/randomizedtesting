package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Determines how instances of the test suite class are created for each test
 * case.
 * 
 * By default JUnit creates a new class instance for every test to prevent test
 * case ordering dependencies. This is sometimes inconvenient as there is no
 * "suite" context other than static fields (which are a nuisance to clean up
 * properly, for example). This annotation changes the default behavior and
 * permits the test cases to be executed on the same instance, for example.
 * 
 * Note that special care should be given to scenarios in which same-instance is
 * reused with arguments provided via {@link ParametersFactory} (each set of
 * parameters will create a separate instance, which will then be used to run
 * all of test suite's test cases).
 * 
 * Note that the same instance will be used if the test cases are multiplied
 * with {@link Seeds} or {@link Repeat} annotations.
 * 
 * @see TestCaseInstanceProvider.Type#INSTANCE_PER_TEST_METHOD
 * @see TestCaseInstanceProvider.Type#INSTANCE_PER_CONSTRUCTOR_ARGS
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TestCaseInstanceProvider {
  public static enum Type {
    /**
     * Each method (test case) will receive a new instance of the class. This is
     * JUnit's default.
     */
    INSTANCE_PER_TEST_METHOD,
    
    /**
     * Each set of constructor arguments (provided from
     * {@link ParametersFactory} or the default empty constructor) creates an
     * instance that is then reused for all of the suite's tests.
     */
    INSTANCE_PER_CONSTRUCTOR_ARGS;
  }
  
  Type value() default Type.INSTANCE_PER_TEST_METHOD;
}
