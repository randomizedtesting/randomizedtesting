package com.carrotsearch.randomizedtesting.examples;

import static com.carrotsearch.randomizedtesting.MethodCollector.allDeclaredMethods;
import static com.carrotsearch.randomizedtesting.MethodCollector.flatten;

import java.lang.reflect.Method;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.ClassValidator;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Validators;
import com.carrotsearch.randomizedtesting.examples.TestValidatorsAnnotation;

/**
 * Test validators on a suite.
 */
@Validators({TestValidatorsAnnotation.NoCursingInMethodNamesValidator.class})
public class TestValidatorsAnnotation extends RandomizedTest {
  public static class NoCursingInMethodNamesValidator implements ClassValidator {
    public void validate(Class<?> clazz) throws Throwable {
      for (Method m : flatten(allDeclaredMethods(clazz))) {
        // We will go lightly here...
        if (m.getName().toLowerCase().contains("arse")) {
          throw new RuntimeException("Stop cursing: " + m.getName()
              + " in class " + clazz.getName());
        }
      }
    }
  }

  @Test
  public void longLiveArsenalLondon() throws Exception {
    // Empty.
  }
}
