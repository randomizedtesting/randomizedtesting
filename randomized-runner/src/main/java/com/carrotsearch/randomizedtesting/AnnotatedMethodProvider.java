package com.carrotsearch.randomizedtesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Base class for {@link TestMethodProvider}s based on annotations.
 */
public abstract class AnnotatedMethodProvider implements TestMethodProvider {
  private final Class<? extends Annotation> annotation;
  
  public AnnotatedMethodProvider(Class<? extends Annotation> ann) {
    this.annotation = ann;
  }

  @Override
  public Collection<Method> getTestMethods(Class<?> suiteClass, ClassModel suiteClassModel) {
    // Return all methods annotated with the given annotation. Rely on further validation
    // to weed out static or otherwise invalid methods.
    return suiteClassModel.getAnnotatedLeafMethods(annotation).keySet();
  }
}
