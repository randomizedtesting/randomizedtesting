package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static com.carrotsearch.randomizedtesting.SysGlobals.*;

/**
 * Check if global filtering works.
 */
public class TestClassMethodFiltering extends WithNestedTestClass {
  static List<String> methods = new ArrayList<String>();

  @RunWith(RandomizedRunner.class)
  public static class Nested1 {
    @BeforeClass
    public static void beforeClass() {
      methods.add("beforeClass1");
    }

    @Test
    public void method1() {
      assumeRunningNested();
      methods.add("method1");
    }

    @Test
    public void method2() {
      assumeRunningNested();
      methods.add("method2");
    }    
  }

  @RunWith(RandomizedRunner.class)
  public static class Nested2 {
    @BeforeClass
    public static void beforeClass() {
      methods.add("beforeClass2");
    }

    @Test
    public void method1() {
      assumeRunningNested();
      methods.add("method1");
    }
  }

  /**
   * Class filter (all methods).
   */
  @Test
  public void testClassFilter() {
    System.setProperty(SYSPROP_TESTCLASS(), Nested1.class.getName());
    runTests(Nested1.class, Nested2.class);
    assertTrue(
        Arrays.asList("beforeClass1", "method1", "method2").equals(methods) ||
        Arrays.asList("beforeClass1", "method2", "method1").equals(methods));
  }

  /**
   * Class and method filter (single method).
   */
  @Test
  public void testClassMethodFilter() {
    System.setProperty(SYSPROP_TESTCLASS(), Nested1.class.getName());
    System.setProperty(SYSPROP_TESTMETHOD(), "method2");
    runTests(Nested1.class, Nested2.class);
    assertEquals(Arrays.asList("beforeClass1", "method2"), methods);
  }

  /**
   * Awkward case: only method filter.
   */
  @Test
  public void testMethodFilter() {
    System.setProperty(SYSPROP_TESTMETHOD(), "method1");
    runTests(Nested1.class, Nested2.class);
    assertEquals(Arrays.asList("beforeClass1", "method1", "beforeClass2", "method1"), methods);
  }

  /**
   * Glob class name filter·
   */
  @Test
  public void testGlobClassName() {
    System.setProperty(SYSPROP_TESTCLASS(), "*Nested1");
    runTests(Nested1.class, Nested2.class);
    assertTrue(
        Arrays.asList("beforeClass1", "method1", "method2").equals(methods) ||
        Arrays.asList("beforeClass1", "method2", "method1").equals(methods));
  }

  /**
   * Glob method name filter·
   */
  @Test
  public void testGlobMethodName() {
    System.setProperty(SYSPROP_TESTMETHOD(), "*hod1");
    runTests(Nested1.class, Nested2.class);
    assertEquals(Arrays.asList("beforeClass1", "method1", "beforeClass2", "method1"), methods);
  }

  @Before
  public void cleanupBefore() {
    cleanupAfter();
  }

  @After
  public void cleanupAfter() {
    System.clearProperty(SYSPROP_TESTCLASS());
    System.clearProperty(SYSPROP_TESTMETHOD());
    methods.clear();
  }
}
