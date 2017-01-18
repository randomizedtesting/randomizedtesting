package com.carrotsearch.randomizedtesting.contracts;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;

/**
 * Hooks ordering with respect to class hierarchies.
 */
@SuppressWarnings("deprecation")
public class TestBeforeAfterMethodOrder extends WithNestedTestClass {
  static final List<String> callOrder = new ArrayList<String>();

  public static class AppendMethodRule implements MethodRule {
    private String text;

    public AppendMethodRule(String text) {
      this.text = text;
    }

    @Override
    public Statement apply(final Statement base, FrameworkMethod method, Object target) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          callOrder.add(text + "-before");
          base.evaluate();
          callOrder.add(text + "-after");
        }
      };
    }
  }
  
  public static class AppendRule implements TestRule {
    private String text;

    public AppendRule(String text) {
      this.text = text;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          callOrder.add(text + "-before");
          base.evaluate();
          callOrder.add(text + "-after");
        }
      };
    }
  }
  
  /**
   * Test superclass.
   */
  public static class Super {
    @BeforeClass
    public static void beforeClassSuper() {
      callOrder.add("beforeClassSuper");
    }

    @Rule
    public TestRule superRule = RuleChain
      .outerRule(new AppendRule("superOuterTestRule"))
      .around(new AppendRule("superMiddleTestRule"))
      .around(new AppendRule("superInnerTestRule"));

    @Rule
    public MethodRule superMethodRule = new AppendMethodRule("superMethodRule");

    @Before
    public final void beforeTest() {
      callOrder.add("beforeTestSuper");
    }

    protected void testMethod() {
      throw new RuntimeException("Should be overridden and public.");
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
    @Rule
    public TestRule rule = RuleChain
      .outerRule(new AppendRule("  subOuterTestRule"))
      .around(new AppendRule("  subMiddleTestRule"))
      .around(new AppendRule("  subInnerTestRule"));

    @Rule
    public MethodRule methodRule = new AppendMethodRule("  subMethodRule");

    @BeforeClass
    public static void beforeClass() {
      callOrder.add("  beforeClassSub");
    }

    @Before
    public void beforeTestSub() {
      callOrder.add("  beforeTestSub");
    }
    
    @Test
    public void testMethod() {
      callOrder.add("    testMethodSub");
    }

    @After
    public void afterTestSub() {
      callOrder.add("  afterTestSub");
    }
    
    @AfterClass
    public static void afterClass() {
      callOrder.add("  afterClassSub");
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
      assumeRunningNested();
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
  public void checkOrder() throws Exception {
    // Normal JUnit.
    checkTestsOutput(1, 0, 0, 0, SubSub.class);
    ArrayList<String> junitOrder = new ArrayList<String>(callOrder);
    
    callOrder.clear();
    new JUnitCore().run(new RandomizedRunner(SubSub.class));

    if (!callOrder.equals(junitOrder)) {
      final int i = junitOrder.size();
      final int j = callOrder.size();
      
      System.out.println(String.format(Locale.ROOT,
          "%-30s | %-30s", "JUnit4", "RR"));
      for (int k = 0; k < Math.max(i, j); k++) {
        System.out.println(String.format(Locale.ROOT,
            "%-30s | %-30s",
            k < i ? junitOrder.get(k) : "--",
            k < j ? callOrder.get(k) : "--"));
      }

      Assert.fail("JUnit4 and RandomizedRunner differed.");
    }
  }

  @Test
  public void checkOrderFixedSeed() throws Exception {
    new JUnitCore().run(new RandomizedRunner(SubSubFixedSeed.class));
    ArrayList<String> order = new ArrayList<String>(callOrder);
    callOrder.clear();
    new JUnitCore().run(new RandomizedRunner(SubSubFixedSeed.class));
    assertEquals(order, callOrder);
  }
}
