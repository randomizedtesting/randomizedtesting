package com.carrotsearch.randomizedtesting;

import com.carrotsearch.randomizedtesting.annotations.ClassValidators;

/**
 * @see ClassValidators
 */
public interface ClassValidator {
  /**
   * Validate the test suite's class and throw anything if not conforming
   * to the needs.
   */
  public void validate(Class<?> clazz) throws Throwable;
}
