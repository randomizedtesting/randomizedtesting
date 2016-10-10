package com.carrotsearch.randomizedtesting.contracts;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

/**
 * {@link ClassRule} support.
 */
public class TestClassRules extends WithNestedTestClass {
  final static List<String> order = new ArrayList<>();
  
  public static class ClassRuleSupport {
    @ClassRule
    public static TestRule rules = RuleChain.outerRule(new TestRule() {
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
    });

    @BeforeClass
    public static void beforeClass() {
      order.add("before-class");
    }

    @AfterClass
    public static void afterClass() {
      order.add("after-class");
    }

    @Test
    public void passing() {
      order.add("passing");
    }
  }
  
  @Test
  public void checkOldMethodRules() throws Exception {
    assertSameExecution(ClassRuleSupport.class);
  }

  private void assertSameExecution(Class<?> clazz) throws Exception {
    order.clear();
    runTests(clazz);
    List<String> order1 = new ArrayList<>(order);
    order.clear();
    
    new JUnitCore().run(Request.runner(new RandomizedRunner(clazz)));
    List<String> order2 = new ArrayList<>(order);
    order.clear();
    
    Assert.assertEquals(order1, order2);
  }
}
