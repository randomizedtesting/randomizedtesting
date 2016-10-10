package com.carrotsearch.randomizedtesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.WithNestedTestClass.FullResult;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

/**
 * Try to be compatible with JUnit's runners wrt method hooks throwing
 * exceptions.
 */
public class TestHookMethodOrderWithExceptions extends RandomizedTest {
  static final List<String> callOrder = new ArrayList<String>();

  /**
   * Test superclass.
   */
  public abstract static class Super {
    static Random rnd; 
    
    @BeforeClass
    public static void beforeClassSuper() {
      callOrder.add("beforeClassSuper");
      maybeThrowException();
    }
    
    @Before
    public final void beforeTest() {
      callOrder.add("beforeTestSuper");
      maybeThrowException();
    }
    
    @After
    public final void afterTest() {
      callOrder.add("afterTestSuper");
      maybeThrowException();

    }
    
    @AfterClass
    public static void afterClassSuper() {
      callOrder.add("afterClassSuper");
      maybeThrowException();
    }
    
    public static void maybeThrowException() {
      if (rnd != null && rnd.nextInt(10) == 0) {
        throw new RuntimeException();
      }
    }
  }
  
  /**
   * Test subclass.
   */
  public static class SubSub extends Super {
    @BeforeClass
    public static void beforeClass() {
      callOrder.add("beforeClassSub");
      maybeThrowException();
    }
    
    @Before
    public void beforeTestSub() {
      callOrder.add("beforeTestSub");
      maybeThrowException();
    }
    
    @Test
    public void testMethod() {
      callOrder.add("testMethodSub");
      maybeThrowException();
    }
    
    @After
    public void afterTestSub() {
      callOrder.add("afterTestSub");
      maybeThrowException();
    }
    
    @AfterClass
    public static void afterClass() {
      callOrder.add("afterClassSub");
      maybeThrowException();
    }
  }
  
  @Before
  public void setup() {
    callOrder.clear();
  }
  
  @After
  public void cleanup() {
    callOrder.clear();
  }

  @RunWith(RandomizedRunner.class)
  public static class WithRandomizedRunner extends SubSub {}
  public static class WithRegularRunner extends SubSub {}
  
  @Test @Repeat(iterations = 20)
  public void checkOrderSameAsJUnit() throws Exception {
    long seed = RandomizedContext.current().getRandomness().getSeed();

    callOrder.clear();
    Super.rnd = new Random(seed);

    FullResult r1 = WithNestedTestClass.runTests(WithRegularRunner.class);
    List<String> junitOrder = new ArrayList<String>(callOrder);

    callOrder.clear();
    Super.rnd = new Random(seed);
    FullResult r2 = WithNestedTestClass.runTests(WithRandomizedRunner.class);
    List<String> rrunnerOrder = new ArrayList<String>(callOrder);

    Assert.assertEquals(junitOrder, rrunnerOrder);
    Assertions.assertThat(r1.getRunCount()).isEqualTo(r2.getRunCount());    
  }
}
