package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.MethodCollector.allDeclaredMethods;
import static com.carrotsearch.randomizedtesting.MethodCollector.flatten;
import static com.carrotsearch.randomizedtesting.MethodCollector.removeOverrides;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

/**
 * Prevent:
 * <ol>
 *   <li>methods that look like JUnit3 by-name convention test cases, but are not annotated
 *   with {@link Test}.</li>
 * </ol>
 */
public class NoJUnit3TestMethods implements ClassValidator {
  @Override
  public void validate(Class<?> clazz) throws Throwable {
    List<List<Method>> all = allDeclaredMethods(clazz);

    for (Method m : flatten(removeOverrides(all))) {
      int modifiers = m.getModifiers();
      if (isPublic(modifiers) && !isStatic(modifiers)) {
        if (m.getName().startsWith("test") && !m.isAnnotationPresent(Test.class)) {
          throw new RuntimeException("Class " +
              clazz.getName() + " has a public instance method starting with 'test' that " +
              "is not annotated with @Test, possibly a dead JUnit3 test case: " +
              m.toGenericString());
        }
      }
    }
  }
}
