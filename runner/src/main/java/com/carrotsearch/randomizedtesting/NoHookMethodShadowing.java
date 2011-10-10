package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.MethodCollector.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Prevent:
 * <ol>
 *   <li>shadowing {@link BeforeClass} and {@link AfterClass} methods (in standard JUnit
 * this causes shadowed methods not to be executed),</li>
 *   <li>overriding {@link Before} and {@link After} methods (which enforces you to 
 *   remember to call the overridden method using <code>super</code>.</li>
 * </ol>
 * 
 * <p>Both shadowing and overriding of hooks can have its legitimate uses, but typically
 * it is just a mistake.
 */
public class NoHookMethodShadowing implements ClassValidator {
  @Override
  public void validate(Class<?> clazz) throws Throwable {
    List<List<Method>> all = allDeclaredMethods(clazz);

    checkNoShadows(clazz, all, BeforeClass.class);
    checkNoShadows(clazz, all, AfterClass.class);
    
    checkNoOverrides(clazz, all, After.class);
    checkNoOverrides(clazz, all, Before.class);
  }

  private void checkNoShadows(Class<?> clazz, List<List<Method>> all, Class<? extends Annotation> ann) {
    List<List<Method>> methodHierarchy = annotatedWith(all, ann);
    List<List<Method>> noShadows = removeShadowed(methodHierarchy);
    if (!noShadows.equals(methodHierarchy)) {
      Set<Method> allMethods = new HashSet<Method>(flatten(methodHierarchy));
      allMethods.removeAll(flatten(noShadows));

      StringBuilder b = new StringBuilder();
      for (Method m : allMethods) {
        if (b.length() > 0) b.append(", ");
        b.append(m.toGenericString());
      }

      throw new RuntimeException("There are shadowed methods annotated with "
          + ann.getName() + ". These methods would not be executed by JUnit and need to manually chain themselves which can lead to" +
          		" maintenance problems. Use unique names for shadowed methods: " + b.toString());
    }
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

      throw new RuntimeException("There are overriden methods annotated with "
          + ann.getName() + ". These methods need to manually call super.xxx() , which can lead to" +
              " maintenance problems. Use unique names for overridden methods: " + b.toString());
    }
  }  
}
