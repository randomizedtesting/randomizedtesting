package com.carrotsearch.randomizedtesting.rules;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

public class TestNoClassHooksShadowingRule extends WithNestedTestClass {
  public static class Super extends RandomizedTest {
    private static TestRule assumeNotNestedRule = new TestRule() {
      public Statement apply(final Statement base, Description description) {
        return new Statement() {
          public void evaluate() throws Throwable {
            assumeRunningNested();
            base.evaluate();
          }
        };
      }
    };
    
    @ClassRule
    public static TestRule classRules = 
      RuleChain
        .outerRule(assumeNotNestedRule)
        .around(new NoClassHooksShadowingRule());

    @BeforeClass
    public static void before() {}

    @BeforeClass
    private static void privateBefore() {}

    @Test
    public void testEmpty() {}
  }

  public static class Sub1 extends Super {
    public static void before() {}
  }

  public static class Sub2 extends Super {
    @BeforeClass
    public static void before() {}
  }

  public static class Sub3 extends Super {
    @BeforeClass
    private static void privateBefore() {}
  }

  @Test
  public void testShadowingNoAnnotation() {
    FullResult runClasses = runTests(Sub1.class);
    Assertions.assertThat(runClasses.getFailures()).isNotEmpty();
    Assertions.assertThat(runClasses.getFailures().get(0).getTrace())
    .contains("shadow or override each other");
  }
  
  @Test
  public void testShadowingWithAnnotation() {
    FullResult runClasses = runTests(Sub2.class);
    Assertions.assertThat(runClasses.getFailures()).isNotEmpty();
    Assertions.assertThat(runClasses.getFailures().get(0).getTrace())
    .contains("shadow or override each other");
  }

  @Test
  public void testIndependentChains() {
    FullResult runClasses = runTests(Sub3.class);
    Assertions.assertThat(runClasses.getFailures()).isEmpty();
  }  
}
