package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.Validators;

/**
 * Test {@link NoHookMethodShadowing}.
 */
public class TestNoHookMethodShadowing extends WithNestedTestClass {

  public static class NoHookMethodShadowing_ extends NoHookMethodShadowing {
    @Override
    public void validate(Class<?> clazz) throws Throwable {
      if (isRunningNested()) {
        super.validate(clazz);
      }
    }
  }

  @Validators({NoHookMethodShadowing_.class})
  public static class Before1 extends RandomizedTest {
    @BeforeClass
    public static void beforeMethod() {}

    @BeforeClass
    public static void anotherMethod() {}    
  }

  public static class Before2 extends Before1 {
    @BeforeClass
    public static void beforeMethod() {}

    @BeforeClass
    public static void yetAnotherMethod() {}        
  }

  @Test
  public void checkBeforeShadowing() {
    Result r = JUnitCore.runClasses(Before2.class);
    assertEquals(1, r.getFailureCount());
    assertTrue(r.getFailures().get(0).getMessage().contains("beforeMethod"));
  }

  @Validators({NoHookMethodShadowing_.class})
  public static class After1 extends RandomizedTest {
    @AfterClass
    public static void afterMethod() {}
  }

  public static class After2 extends After1 {
    @AfterClass
    public static void afterMethod() {}
  }
  
  @Test
  public void checkAfterShadowing() {
    Result r = JUnitCore.runClasses(After2.class);
    assertEquals(1, r.getFailureCount());
    assertTrue(r.getFailures().get(0).getMessage().contains("afterMethod"));
  }
  
  @Validators({NoHookMethodShadowing_.class})
  public static class Before3 extends RandomizedTest {
    @Before
    public void beforeMethod() {}    
  }

  public static class Before4 extends Before3 {
    @Before
    public void beforeMethod() {}
  }

  @Test
  public void checkBeforeOverride() {
    Result r = JUnitCore.runClasses(Before4.class);
    assertEquals(1, r.getFailureCount());
    assertTrue(r.getFailures().get(0).getMessage().contains("beforeMethod"));
  }  
}
