package com.carrotsearch.randomizedtesting.validators;

import javax.xml.bind.ValidationException;

import com.carrotsearch.randomizedtesting.ClassValidator;

/**
 * Prevent:
 * <ol>
 *   <li>execution without enabled (global) assertions (<code>-ea</code>).</li>
 * </ol>
 */
public class EnsureAssertionsEnabled implements ClassValidator {
  @Override
  public void validate(Class<?> clazz) throws Throwable {
    try {
      assert false;
      throw new ValidationException("Enable assertions globally with -ea (currently disabled or filtered to selected packages).");
    } catch (AssertionError e) {
      // Ok, enabled.
    }
  }
}
