package com.carrotsearch.randomizedtesting;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    Map<Method, ClassModel.MethodModel> methods = suiteClassModel.getAnnotatedLeafMethods(annotation);
    if (annotation.equals(Test.class)) {
      // @Test doesn't have inherited attribute, yet behaves like one. Fix it here by returning most
      // specific method with a parent marked @Test
      List<Method> fixed = new ArrayList<Method>();
      for (ClassModel.MethodModel mm : methods.values()) {
        // find the most specific method.
        ClassModel.MethodModel override;
        while ((override = mm.getDown()) != null) {
          mm = override;
        }

        fixed.add(mm.element);
      }
      return fixed;
    }
    return methods.keySet();
  }
}
