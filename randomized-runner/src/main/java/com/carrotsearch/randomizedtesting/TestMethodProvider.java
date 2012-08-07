package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Responsible for providing individual test instances and their descriptions. Also
 * performs class validation to ensure test methods are valid.
 */
public interface TestMethodProvider {
  /**
   * Determine which methods are test methods. The contract is that methods must
   * be public, instance bound (not static) and parameterless. No other
   * restrictions apply (as if these weren't enough...).
   * 
   * @param suiteClass
   *          The suite class.
   * @param suiteClassModel
   *          A precomputed model of the suite class including method annotations and
   *          class hierarchy walking utilities. This is made available for performance
   *          reasons only.
   * @return Return a set of methods which should be invoked by the runner as
   *         tests.
   */
  Collection<Method> getTestMethods(Class<?> suiteClass, ClassModel suiteClassModel);
}
