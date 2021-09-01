package com.carrotsearch.randomizedtesting.engine;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static org.junit.platform.commons.util.ReflectionUtils.*;

class Predicates {
  static class IsTestableMethod implements Predicate<Method> {
    private final boolean mustReturnVoid;

    IsTestableMethod(boolean mustReturnVoid) {
      this.mustReturnVoid = mustReturnVoid;
    }

    @Override
    public boolean test(Method candidate) {
      if (isStatic(candidate) || isPrivate(candidate) || isAbstract(candidate)) {
        return false;
      }
      if (returnsVoid(candidate) != this.mustReturnVoid) {
        return false;
      }

      return candidate.getName().startsWith("test");
    }
  }

  static class IsPotentialTestContainer implements Predicate<Class<?>> {

    @Override
    public boolean test(Class<?> candidate) {
      // Please do not collapse the following into a single statement.
      if (isPrivate(candidate)) {
        return false;
      }
      if (isAbstract(candidate)) {
        return false;
      }
      if (candidate.isLocalClass()) {
        return false;
      }
      if (candidate.isAnonymousClass()) {
        return false;
      }
      return !isInnerClass(candidate);
    }
  }

  static class IsTestClassWithTests implements Predicate<Class<?>> {
    private static final IsTestableMethod isTestMethod = new IsTestableMethod(true);

    private static final IsPotentialTestContainer isPotentialTestContainer =
        new IsPotentialTestContainer();

    @Override
    public boolean test(Class<?> candidate) {
      return isPotentialTestContainer.test(candidate);
    }
  }
}
