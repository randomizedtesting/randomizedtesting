package com.carrotsearch.randomizedtesting.validators;

import static com.carrotsearch.randomizedtesting.MethodCollector.allDeclaredMethods;
import static com.carrotsearch.randomizedtesting.MethodCollector.annotatedWith;
import static com.carrotsearch.randomizedtesting.MethodCollector.flatten;
import static com.carrotsearch.randomizedtesting.MethodCollector.removeOverrides;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.ClassValidator;

/**
 * Prevent:
 * <ol>
 *   <li>overriding {@link Test} annotated methods. Hiding or changing a test
 *   case's functionality is bad practice. Instead add an assumption on something (to
 *   disable a test case in subclasses) or use a non-test protected method to extract
 *   common functionality.</li>
 * </ol>
 */
public class NoTestMethodOverrides implements ClassValidator {
  @Override
  public void validate(Class<?> clazz) throws Throwable {
    List<List<Method>> all = allDeclaredMethods(clazz);

    checkNoOverrides(clazz, all, Test.class);
  }
  
  private void checkNoOverrides(Class<?> clazz, List<List<Method>> all, Class<? extends Annotation> ann) {
    List<List<Method>> methodHierarchy = annotatedWith(all, ann);
    List<List<Method>> noOverrides = removeOverrides(methodHierarchy);
    if (!noOverrides.equals(methodHierarchy)) {
      Set<Method> allMethods = new HashSet<Method>(flatten(methodHierarchy));
      allMethods.removeAll(flatten(noOverrides));

      StringBuilder b = new StringBuilder();
      for (Method m : allMethods) {
        if (b.length() > 0) b.append(", ");
        b.append(m.toGenericString());
      }

      throw new RuntimeException("There are overridden methods annotated with "
          + ann.getName()
          + ". This is considered bad practice. See javadoc for" + " "
          + NoTestMethodOverrides.class.getName()
          + " class for more info. Overridden test methods: " + b.toString());
    }
  }
}
