package com.carrotsearch.randomizedtesting.contracts;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

/**
 * Verify if annotations are inherited.
 */
public class TestAnnotationInheritance extends WithNestedTestClass {
  final static List<String> order = new ArrayList<>();
  
  public static class Nested1 {
    @Rule
    public TestRule rules = new TestRule() {
      @Override
      public Statement apply(final Statement base, Description description) {
        return new Statement() {
          public void evaluate() throws Throwable {
            order.add("rule-before");
            base.evaluate();
            order.add("rule-after");
          }
        };
      }
    };

    @BeforeClass
    public static void beforeClass() {
      order.add("before-class");
    }

    @Before
    public void before() {
      order.add("before-test");
    }
    
    @Test
    public void testMethod1() {
      order.add("testMethod1");
    }

    @After
    public void after() {
      order.add("after-test");
    }
    
    @AfterClass
    public static void afterClass() {
      order.add("after-class");
    }
  }
  
  public static class Nested2 extends Nested1 {
    public static void beforeClass() {
      order.add("shadowed-before-class");
    }

    @Override
    public void before() {
      order.add("inherited before-test");
    }
    
    @Override
    public void after() {
      order.add("inherited after-test");
    }
    
    @Override
    public void testMethod1() {
      order.add("inherited testMethod");
    }

    public static void afterClass() {
      order.add("shadowed-after-class");
    }    
  }

  @Test
  public void checkOldMethodRules() throws Exception {
    assertSameExecution(Nested2.class);
  }

  private void assertSameExecution(Class<?> clazz) throws Exception {
    order.clear();
    runTests(clazz);
    List<String> order1 = new ArrayList<>(order);
    order.clear();

    new JUnitCore().run(Request.runner(new RandomizedRunner(clazz)));
    List<String> order2 = new ArrayList<>(order);
    order.clear();

    String msg = "# JUnit order:\n" + order1 + "\n" +
                 "# RR order:\n" + order2;
    Assertions.assertThat(order2).as(msg).isEqualTo(order1);
  }
}
