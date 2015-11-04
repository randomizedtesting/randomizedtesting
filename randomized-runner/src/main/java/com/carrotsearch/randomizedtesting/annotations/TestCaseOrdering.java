package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

import com.carrotsearch.randomizedtesting.TestMethodAndParams;

/**
 * Test case ordering. The returned comparator will be used for sorting 
 * all {@link TestMethodAndParams} entries, where method is the test method
 * and params are potential parameters to the constructor for a given
 * test (if {@link ParametersFactory} is used).
 * 
 * The sort is stable with respect to the original (shuffled) order of
 * test cases.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TestCaseOrdering {
  /**
   * The comparator used to sort all test cases.
   * 
   * @see TestCaseOrdering
   */
  Class<? extends Comparator<TestMethodAndParams>> value();

  /**
   * Alphabetic, increasing order by method name.
   */
  public static class AlphabeticOrder implements Comparator<TestMethodAndParams> {
    @Override
    public int compare(TestMethodAndParams o1, TestMethodAndParams o2) {
      return o1.getTestMethod().getName().compareTo(
             o2.getTestMethod().getName());
    }
  }
}
