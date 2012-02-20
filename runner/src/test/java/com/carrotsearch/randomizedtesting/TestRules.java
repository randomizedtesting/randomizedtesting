package com.carrotsearch.randomizedtesting;

import java.util.List;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.common.collect.Lists;

/**
 * {@link Rule} and {@link MethodRule} support.
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class TestRules extends WithNestedTestClass {
  static List<String> order;

  public static class MethodRuleSupport {
    @Rule
    public MethodRule rule1 = new MethodRule() {
      public Statement apply(final Statement base, FrameworkMethod method, Object target) {
        return new Statement() {
          @Override
          public void evaluate() throws Throwable {
            assumeRunningNested();
            order.add("rule1-before");
            base.evaluate();
            order.add("rule1-after");
          }
        };
      }
    };

    @Test
    public void passing() {
      order.add("passing");
    }
  }

  public static class SimpleRule implements TestRule {
    private String msg;

    public SimpleRule(String msg) {
      this.msg = msg;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
      return new Statement() {
        public void evaluate() throws Throwable {
          assumeRunningNested();
          order.add(msg + "-before");
          try {
            base.evaluate();
          } finally {
            order.add(msg + "-after");
          }
        }
      };
    }
  }

  public static class NewRuleSupportPassing {
    @Rule
    public TestRule rule1 = RuleChain
      .outerRule(new SimpleRule("outer"))
      .around(new SimpleRule("inner"));

    @Test
    public void passing() {
      order.add("passing");
    }
  }

  public static class NewRuleSupportFailing {
    @Rule
    public TestRule rule1 = RuleChain
      .outerRule(new SimpleRule("outer"))
      .around(new SimpleRule("inner"));

    @Test
    public void failing() {
      order.add("failing");
      throw new RuntimeException();
    }
  }


  public static class NewRuleSupportIgnored {
    @Rule
    public TestRule rule1 = RuleChain
      .outerRule(new SimpleRule("outer"))
      .around(new SimpleRule("inner"));

    @Test @Ignore
    public void ignored() {
      order.add("ignored");
      throw new RuntimeException();
    }        
  }

  @Test
  public void checkOldMethodRules() throws Exception {
    assertSameExecution(MethodRuleSupport.class);
  }

  @Test
  public void checkNewTestRules() throws Exception {
    assertSameExecution(NewRuleSupportPassing.class);
    assertSameExecution(NewRuleSupportFailing.class);
    assertSameExecution(NewRuleSupportIgnored.class);
  }

  private void assertSameExecution(Class<?> clazz) throws Exception {
    order = Lists.newArrayList();
    JUnitCore.runClasses(clazz);
    List<String> order1 = order;
    order = Lists.newArrayList();

    new JUnitCore().run(Request.runner(new RandomizedRunner(clazz)));
    List<String> order2 = order;
    order = null;

    Assert.assertEquals(order1, order2);
  }
}
