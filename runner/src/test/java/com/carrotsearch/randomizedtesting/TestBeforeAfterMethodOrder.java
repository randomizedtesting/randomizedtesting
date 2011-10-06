package com.carrotsearch.randomizedtesting;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Before and after hooks order with a class hierarchy.
 */
public class TestBeforeAfterMethodOrder {
  /**
   * Test superclass.
   */
  public static class Super {
    protected static int counter;

    public static int beforeClassSuperOrder;
    public static int beforeTestSuperOrder;
    public static int beforeClassSubOrder;
    public static int beforeTestSubOrder;
    public static int testMethodOrderSub;
    public static int afterTestSubOrder;
    public static int afterClassSubOrder;
    public static int afterTestSuperOrder;
    public static int afterClassSuperOrder;

    @BeforeClass
    public static void beforeClassSuper() {
      beforeClassSuperOrder = ++counter;
    }

    @Before
    public final void beforeTest() {
      beforeTestSuperOrder = ++counter;
    }

    @After
    public final void afterTest() {
      afterTestSuperOrder = ++counter;
    }
    
    @AfterClass
    public static void afterClassSuper() {
      afterClassSuperOrder = ++counter;
    }
  }

  /** 
   * Test subclass.
   */
  @RunWith(RandomizedRunner.class)
  public static class SubSub extends Super {
    @BeforeClass
    public static void beforeClass() {
      beforeClassSubOrder = ++counter;
    }

    @Before
    public void beforeTestSub() {
      beforeTestSubOrder = ++counter;
    }
    
    @Test
    public void testMethod() {
      testMethodOrderSub = ++counter;
    }

    @After
    public void afterTestSub() {
      afterTestSubOrder = ++counter;
    }
    
    @AfterClass
    public static void afterClass() {
      afterClassSubOrder = ++counter;
    }
  }

  @Before 
  public void cleanup() {
    Super.beforeClassSuperOrder = 0;
    Super.beforeClassSubOrder = 0;
    Super.beforeTestSuperOrder = 0;
    Super.beforeTestSubOrder = 0;
    Super.testMethodOrderSub = 0;
    Super.afterTestSubOrder = 0;
    Super.afterTestSuperOrder = 0;
    Super.afterClassSubOrder = 0;
    Super.afterClassSuperOrder = 0;
    Super.counter = 0;
  }
  
  @Test
  public void beforesCalled() {
    Result result = JUnitCore.runClasses(SubSub.class);

    assertEquals(1, result.getRunCount());

    assertEquals(1, Super.beforeClassSuperOrder);
    assertEquals(2, Super.beforeClassSubOrder);
    assertEquals(3, Super.beforeTestSuperOrder);
    assertEquals(4, Super.beforeTestSubOrder);
    assertEquals(5, Super.testMethodOrderSub);
    assertEquals(6, Super.afterTestSubOrder);
    assertEquals(7, Super.afterTestSuperOrder);
    assertEquals(8, Super.afterClassSubOrder);
    assertEquals(9, Super.afterClassSuperOrder);
  }
}
