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
 * Tests {@link RandomizedRunner}
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
    public static int testMethodOrderSuper;
    public static int testMethodOrderSub;
    public static int afterTestSubOrder;
    public static int afterClassSubOrder;
    public static int afterTestSuperOrder;
    public static int afterClassSuperOrder;

    @BeforeClass
    public static void beforeClass() {
      beforeClassSuperOrder = ++counter;
    }

    @Before
    public final void beforeTest() {
      beforeTestSuperOrder = ++counter;
    }

    @Test
    public void testMethodSuper() {
      testMethodOrderSuper = ++counter;
    }    

    @After
    public final void afterTest() {
      afterTestSuperOrder = ++counter;
    }
    
    @AfterClass
    public static void afterClass() {
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

  @Test
  public void beforesCalled() {
    Result result = JUnitCore.runClasses(SubSub.class);

    assertEquals(2, result.getRunCount());
    assertEquals(1, Super.beforeClassSuperOrder);
    assertEquals(2, Super.beforeClassSubOrder);
    assertEquals(3, Super.beforeTestSuperOrder);
    assertEquals(4, Super.beforeTestSubOrder);
    assertTrue(Super.testMethodOrderSub > 3);
    assertTrue(Super.testMethodOrderSuper > 3);
    assertEquals(7, Super.afterTestSubOrder);
    assertEquals(8, Super.afterTestSuperOrder);
    assertEquals(9, Super.afterClassSubOrder);
    assertEquals(10, Super.afterClassSuperOrder);
  }
}
