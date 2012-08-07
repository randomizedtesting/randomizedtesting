package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.carrotsearch.randomizedtesting.ClassModel.MethodModel;

/**
 * Method provider selecting tests that follow a name pattern of <code>test(.*)</code>.
 */
public class JUnit3MethodProvider implements TestMethodProvider {
  @Override
  public Collection<Method> getTestMethods(Class<?> suiteClass, ClassModel suiteClassModel) {
    Map<Method,MethodModel> methods = suiteClassModel.getMethods();
    ArrayList<Method> result = new ArrayList<Method>();
    for (MethodModel mm : methods.values()) {
      if (mm.getDown() == null && mm.element.getName().startsWith("test")) {
        result.add(mm.element);
      }
    }
    return result;
  }
}
