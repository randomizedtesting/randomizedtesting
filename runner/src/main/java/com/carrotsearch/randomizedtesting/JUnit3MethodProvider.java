package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.MethodCollector.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Method provider selecting tests that follow a name pattern of <code>test(.*)</code>.
 */
public class JUnit3MethodProvider implements TestMethodProvider {
  @Override
  public Collection<Method> getTestMethods(Class<?> suiteClass, List<List<Method>> methods) {
    // We will return all methods starting with test* and rely on further validation to weed
    // out static or otherwise invalid test methods.
    List<Method> onlyPublic = mutableCopy1(flatten(methods));
    Iterator<Method> i = onlyPublic.iterator();
    while (i.hasNext()) {
      if (!i.next().getName().startsWith("test")) {
        i.remove();
      }
    }
    return onlyPublic;
  }
}
