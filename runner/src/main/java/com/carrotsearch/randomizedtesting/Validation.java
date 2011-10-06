package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Validation utilities. Chained-call style.
 */
final class Validation {
  
  static final class MethodValidation {
    private final Method m;
    private String description;

    public MethodValidation(Method m) {
      this.m = m;
      this.description = "Method " + m.getName();
    }

    public MethodValidation isPublic() {
      if (!Modifier.isPublic(m.getModifiers())) {
        throw new RuntimeException(description + " should be public.");
      }
      return this;
    }

    public MethodValidation describedAs(String message) {
      this.description = message;
      return this;
    }

    public MethodValidation hasArgsCount(int argsCount) {
      if (m.getParameterTypes().length != argsCount) {
        throw new RuntimeException(description + " should have " + argsCount + " parameters and" +
        		" has these: " + Arrays.toString(m.getParameterTypes()));
      }
      return this;
    }
  }
  
  public static MethodValidation checkThat(Method method) {
    return new MethodValidation(method);
  }
}
