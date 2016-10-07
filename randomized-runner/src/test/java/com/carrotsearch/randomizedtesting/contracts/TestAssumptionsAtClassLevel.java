package com.carrotsearch.randomizedtesting.contracts;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

/**
 * Check assumptions at suite level (in {@link BeforeClass}).
 */
public class TestAssumptionsAtClassLevel extends WithNestedTestClass {
  static final List<String> callOrder = new ArrayList<String>();

  /**
   * Test superclass.
   */
  public static class Super extends RandomizedTest {
    @BeforeClass
    public static void beforeClassSuper() {
      callOrder.add("beforeClassSuper");
      assumeRunningNested();
      Assume.assumeTrue(false);
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

  @Before
  public void cleanup() {
    callOrder.clear();
  }

  @Test
  public void checkOrder() {
    Result result = runClasses(SubSub.class);

    assertEquals(0, result.getRunCount());
    assertEquals(1, result.getIgnoreCount());
    assertEquals(0, result.getFailureCount());

    List<String> expected = Arrays.asList(
        "beforeClassSuper",
        "afterClassSub",
        "afterClassSuper"
    );
    assertEquals(expected, callOrder);
  }
}
