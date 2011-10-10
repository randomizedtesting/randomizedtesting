package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Validators;

/**
 * Test {@link NoTestMethodOverrides}.
 */
public class TestNoTestMethodOverrides extends WithNestedTestClass {

  public static class NoTestMethodOverrides_ extends NoTestMethodOverrides {
    @Override
    public void validate(Class<?> clazz) throws Throwable {
      if (isRunningNested()) {
        super.validate(clazz);
      }
    }
  }

  @Validators({NoTestMethodOverrides_.class})
  public static class Nested1 extends RandomizedTest {
    @Test
    public void method1() {}

    @Test
    public void method2() {}
  }

  public static class Nested2 extends Nested1 {
    @Test @Ignore @Override
    public void method1() {}

    @Test @Override
    public void method2() {}
  }

  @Test
  public void checkTestOverride() {
    Result r = JUnitCore.runClasses(Nested2.class);
    assertEquals(1, r.getFailureCount());
    assertTrue(r.getFailures().get(0).getMessage().contains("method1"));
    assertTrue(r.getFailures().get(0).getMessage().contains("method2"));
  }  
}
