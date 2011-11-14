package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Validation utilities. Chained-call style.
 */
final class Validation {
  
  public static final class MethodValidation {
    private final Method m;
    private String description;

    public MethodValidation(Method m) {
      this.m = m;
      this.description = "Method " + m.getName();
    }

    public MethodValidation describedAs(String message) {
      this.description = message;
      return this;
    }

    public MethodValidation isPublic() {
      if (!Modifier.isPublic(m.getModifiers())) {
        throw new RuntimeException(description + " should be public.");
      }
      return this;
    }

    public MethodValidation hasArgsCount(int argsCount) {
      if (m.getParameterTypes().length != argsCount) {
        throw new RuntimeException(description + " should have " + argsCount + " parameters and" +
        		" has these: " + Arrays.toString(m.getParameterTypes()));
      }
      return this;
    }

    public MethodValidation isStatic() {
      if (!Modifier.isStatic(m.getModifiers())) {
        throw new RuntimeException(description + " should be static.");
      }
      return this;
    }

    public MethodValidation isNotStatic() {
      if (Modifier.isStatic(m.getModifiers())) {
        throw new RuntimeException(description + " should be instance method (not static).");
      }
      return this;
    }

    public MethodValidation hasReturnType(Class<?> clazz) {
      if (!clazz.isAssignableFrom(m.getReturnType())) {        
        throw new RuntimeException(description + " should have a return " +
        		"type assignable to: " + clazz.getName());
      }
      return this;
    }
  }

  public static final class ClassValidation {
    private final Class<?> clazz;
    private String description;

    public ClassValidation(Class<?> clazz) {
      this.clazz = clazz;
      this.description = "Class " + clazz.getName();
    }

    public ClassValidation describedAs(String message) {
      this.description = message;
      return this;
    }

    public ClassValidation isPublic() {
      if (!Modifier.isPublic(clazz.getModifiers())) {
        throw new RuntimeException(description + " should be public.");
      }
      return this;
    }

    public ClassValidation isConcreteClass() {
      if (Modifier.isInterface(clazz.getModifiers())) {
        throw new RuntimeException(description + " should be a conrete class (not an interface).");
      }
      if (Modifier.isAbstract(clazz.getModifiers())) {
        throw new RuntimeException(description + " should be a concrete class (not abstract).");
      }
      return this;
    }

    public void hasPublicNoArgsConstructor() {
      try {
        clazz.getConstructor(new Class [0]);
      } catch (Throwable e) {
        throw new RuntimeException(description + " should have a public parameterless constructor.");
      }
    }
  }

  public static MethodValidation checkThat(Method method) {
    return new MethodValidation(method);
  }

  public static ClassValidation checkThat(Class<?> clazz) {
    return new ClassValidation(clazz);
  }
}
