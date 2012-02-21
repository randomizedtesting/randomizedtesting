package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.MethodCollector.annotatedWith;
import static com.carrotsearch.randomizedtesting.MethodCollector.flatten;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Base class for {@link TestMethodProvider}s based on annotations.
 */
public abstract class AnnotatedMethodProvider implements TestMethodProvider {
  private final Class<? extends Annotation> annotation;
  
  public AnnotatedMethodProvider(Class<? extends Annotation> ann) {
    this.annotation = ann;
  }

  @Override
  public Collection<Method> getTestMethods(Class<?> suiteClass, List<List<Method>> methods) {
    // We will return all methods starting with test* and rely on further validation to weed
    // out static or otherwise invalid test methods.
    return flatten(annotatedWith(methods, annotation));
  }
}
