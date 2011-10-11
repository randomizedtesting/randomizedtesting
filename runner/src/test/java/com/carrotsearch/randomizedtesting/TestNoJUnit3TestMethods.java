package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Validators;

/**
 * Test {@link NoHookMethodShadowing}.
 */
public class TestNoJUnit3TestMethods extends WithNestedTestClass {

  public static class NoJUnit3TestMethods_ extends NoJUnit3TestMethods {
    @Override
    public void validate(Class<?> clazz) throws Throwable {
      if (isRunningNested()) {
        super.validate(clazz);
      }
    }
  }

  @Validators({NoJUnit3TestMethods_.class})
  public static class Nested extends RandomizedTest {
    public void testIAmNot() {}
  }
 
  @Test
  public void checkAfterShadowing() {
    Result r = JUnitCore.runClasses(Nested.class);
    assertEquals(1, r.getFailureCount());
    assertTrue(r.getFailures().get(0).getMessage().contains("testIAmNot"));
  }
}
