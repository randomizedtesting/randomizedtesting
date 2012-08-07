package com.carrotsearch.examples.randomizedrunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.carrotsearch.randomizedtesting.ClassModel;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.TestMethodProvider;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.TestMethodProviders;

/**
 * Because many people are nearly religious about how test methods
 * should be annotated or structured {@link RandomizedRunner} can use 
 * a custom method selector for designating test methods.
 * 
 * <p>This class contains a method selector that returns all methods
 * that <b>end</b> with a substring <code>Test</code>.
 */
public class Test015CustomMethodProviders {

  public static class MethodEndsWithTest implements TestMethodProvider {
    @Override
    public Collection<Method> getTestMethods(Class<?> clz, ClassModel suiteClassModel) {
      /*
       * We pick all methods with a "Test" suffix. We also skip methods belonging to 
       * RandomizedTest (there is a private method ending in Test there and this wouldn't
       * validate). Additional validation is performed in the runner (public, non-static,
       * no-args methods only allowed).
       */
      List<Method> result = new ArrayList<Method>();
      for (Method m : suiteClassModel.getMethods().keySet()) {
        if (m.getName().endsWith("Test") && !m.getDeclaringClass().equals(RandomizedTest.class)) {
          result.add(m);
        }
      }
      return result;
    }
  }

  @TestMethodProviders({
    MethodEndsWithTest.class
  })
  public static class TestClass extends RandomizedTest {
    public void myFirstTest() {
      System.out.println("First test.");
    }

    @Repeat(iterations = 5)
    public void mySecondTest() {
      System.out.println("Second test.");
    }
  }
}
