package com.carrotsearch.randomizedtesting;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestValidation extends WithNestedTestClass {
  public class SuiteClassNotStatic {
  }

  static class SuiteClassNotPublic {
  }

  public static class BeforeClassNotStatic {
    @BeforeClass
    public void beforeClass() {
    }
  }

  public static class BeforeClassWithArgs {
    @BeforeClass
    public static void beforeClass(int a) {
    }
  }

  public static class AfterClassNotStatic {
    @AfterClass
    public void afterClass() {
    }
  }

  public static class AfterClassWithArgs {
    @AfterClass
    public static void afterClass(int a) {
    }
  }

  public static class BeforeStatic {
    @Before
    public static void before() {
    }
  }

  public static class BeforeWithArgs {
    @Before
    public void before(int a) {
    }
  }

  public static class AfterStatic {
    @After
    public static void after() {
    }
  }

  public static class AfterWithArgs {
    @After
    public void after(int a) {
    }
  }

  @Test
  public void checkBeforeClass() throws Exception {
    for (Class<?> c : Arrays.asList(
        SuiteClassNotPublic.class, SuiteClassNotStatic.class,
        BeforeClassNotStatic.class, BeforeClassWithArgs.class,
        AfterClassNotStatic.class, AfterClassWithArgs.class,
        BeforeStatic.class, BeforeWithArgs.class,
        AfterStatic.class, AfterWithArgs.class)) {
      try {
        new RandomizedRunner(c);
        Assert.fail("Expected validation failure on: " + c.getName());
      } catch (Exception e) {
        // Ok, expected.
      }
    }
  }
}
