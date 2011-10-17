package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;

/**
 * Before and after hooks order with a class hierarchy.
 */
public class TestBeforeAfterMethodOrder {
  static final List<String> callOrder = new ArrayList<String>();
  
  /**
   * Test superclass.
   */
  @RunWith(RandomizedRunner.class)
  public static class Super {
    @BeforeClass
    public static void beforeClassSuper() {
      callOrder.add("beforeClassSuper");
    }

    @Before
    public final void beforeTest() {
      callOrder.add("beforeTestSuper");
    }

    protected void testMethod() {
      throw new RuntimeException("Should be overriden and public.");
    }

    @After
    public final void afterTest() {
      callOrder.add("afterTestSuper");
    }
    
    @AfterClass
    public static void afterClassSuper() {
      callOrder.add("afterClassSuper");
    }
  }

  /** 
   * Test subclass.
   */
  public static class SubSub extends Super {
    @BeforeClass
    public static void beforeClass() {
      callOrder.add("beforeClassSub");
    }

    @Before
    public void beforeTestSub() {
      callOrder.add("beforeTestSub");
    }
    
    @Test
    public void testMethod() {
      callOrder.add("testMethodSub");
    }

    @After
    public void afterTestSub() {
      callOrder.add("afterTestSub");
    }
    
    @AfterClass
    public static void afterClass() {
      callOrder.add("afterClassSub");
    }
  }

  /** 
   * Test subclass.
   */
  @Seed("deadbeef")
  public static class SubSubFixedSeed extends Super {
    @BeforeClass
    public static void beforeClass() {
      callOrder.add("beforeClassSubFS");
    }

    @Before
    public void beforeTestSub() {
      callOrder.add("beforeTestSubFS");
    }

    @Test @Repeat(iterations = 10)
    public void testMethod1() {
      callOrder.add("testMethodSubFS1 " 
          + RandomizedContext.current().getRandom().nextInt());
    }

    @Test @Repeat(iterations = 10)
    public void testMethod2() {
      callOrder.add("testMethodSubFS2 " 
          + RandomizedContext.current().getRandom().nextInt());
    }

    @After
    public void afterTestSub() {
      callOrder.add("afterTestSubFS");
    }
    
    @AfterClass
    public static void afterClass() {
      callOrder.add("afterClassSubFS");
    }
  }

  @Before
  public void cleanup() {
    callOrder.clear();
  }

  @Test
  public void checkOrder() {
    Result result = JUnitCore.runClasses(SubSub.class);

    assertEquals(1, result.getRunCount());

    List<String> expected = Arrays.asList(
        "beforeClassSuper",
        "beforeClassSub",
        "beforeTestSuper",
        "beforeTestSub",
        "testMethodSub",
        "afterTestSub",
        "afterTestSuper",
        "afterClassSub",
        "afterClassSuper"
    );
    assertEquals(expected, callOrder);
  }

  @Test
  public void checkOrderFixedSeed() {
    JUnitCore.runClasses(SubSubFixedSeed.class);
    ArrayList<String> order = new ArrayList<String>(callOrder);
    callOrder.clear();
    JUnitCore.runClasses(SubSubFixedSeed.class);
    assertEquals(order, callOrder);
  }
}
