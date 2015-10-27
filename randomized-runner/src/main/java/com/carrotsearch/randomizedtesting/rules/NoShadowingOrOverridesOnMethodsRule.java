package com.carrotsearch.randomizedtesting.rules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.ClassModel;
import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.ClassModel.MethodModel;

/**
 * Discovers shadowing or override relationships among methods annotated with any of the
 * provided annotations.
 */
public abstract class NoShadowingOrOverridesOnMethodsRule implements TestRule {
  private Class<? extends Annotation>[] annotations;

  @SafeVarargs
  public NoShadowingOrOverridesOnMethodsRule(Class<? extends Annotation>... annotations) {
    this.annotations = annotations;
  }

  @Override
  public final Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Class<?> testClass;
        try {
         testClass = RandomizedContext.current().getTargetClass(); 
        } catch (Throwable t) {
          testClass = description.getTestClass();
        }

        validate(testClass);
        base.evaluate();
      }
    };
  }

  public final void validate(Class<?> clazz) throws Throwable {
    ClassModel classModel = new ClassModel(clazz);

    for (Class<? extends Annotation> annClass : annotations) {
      checkNoShadowsOrOverrides(clazz, classModel, annClass);
    }
  }

  private void checkNoShadowsOrOverrides(Class<?> clazz, ClassModel classModel, Class<? extends Annotation> ann) {
    Map<Method,MethodModel> annotatedLeafMethods = classModel.getAnnotatedLeafMethods(ann);

    StringBuilder b = new StringBuilder();
    for (Map.Entry<Method,MethodModel> e : annotatedLeafMethods.entrySet()) {
      if (verify(e.getKey())) {
        MethodModel mm = e.getValue();
        if (mm.getDown() != null || mm.getUp() != null) {
          b.append("Methods annotated with @" + ann.getName() + " shadow or override each other:\n");
          while (mm.getUp() != null) {
            mm = mm.getUp();
          }
          while (mm != null) {
            b.append("  - ");
            if (mm.element.isAnnotationPresent(ann)) b.append("@").append(ann.getSimpleName()).append(" ");
            b.append(signature(mm.element)).append("\n");
            mm = mm.getDown();
          }
        }
      }
    }

    if (b.length() > 0) {
      throw new RuntimeException("There are overridden methods annotated with "
          + ann.getName() + ". These methods would not be executed by JUnit and need to manually chain themselves which can lead to" +
              " maintenance problems. Consider using different method names or make hook methods private.\n" + b.toString().trim());
    }
  }

  protected boolean verify(Method key) {
    return true;
  }

  private String signature(Method m) {
    return m.toString();
  }
}
