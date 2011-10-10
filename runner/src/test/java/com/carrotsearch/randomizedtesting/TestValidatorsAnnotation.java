package com.carrotsearch.randomizedtesting;

import static com.carrotsearch.randomizedtesting.MethodCollector.allDeclaredMethods;
import static com.carrotsearch.randomizedtesting.MethodCollector.flatten;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.ClassValidators;

/**
 * Test validators on a suite.
 */
public class TestValidatorsAnnotation extends WithNestedTestClass {
  static boolean doCheck = false;
  
  public static class NoCursingInMethodNamesValidator implements ClassValidator {
    public void validate(Class<?> clazz) throws Throwable {
      // Don't validate if executed directly by Eclipse runner...
      if (!doCheck) return;

      // This validates _all_ methods, anywhere (private non-test methods too).
      for (Method m : flatten(allDeclaredMethods(clazz))) {
        // We will go lightly here...
        if (m.getName().toLowerCase().contains("arse")) {
          throw new RuntimeException("Methods must not contain curse words: "
              + m.getName() + " in class " + clazz.getName());
        }        
      }
    }
  }

  @ClassValidators({NoCursingInMethodNamesValidator.class})
  public static class BadTestClass extends RandomizedTest {
    @Test
    public void longLiveArsenalLondon() throws Exception {
      // Empty.
    }
  }

  @Test
  public void checkClassValidator() {
    doCheck = true;
    Result runClasses = JUnitCore.runClasses(BadTestClass.class);
    Assert.assertEquals(1, runClasses.getFailureCount());
    Assert.assertEquals(0, runClasses.getRunCount());
    doCheck = false;
  }
}
