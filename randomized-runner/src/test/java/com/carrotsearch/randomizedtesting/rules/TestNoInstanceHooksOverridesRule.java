package com.carrotsearch.randomizedtesting.rules;

import java.lang.reflect.Method;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

public class TestNoInstanceHooksOverridesRule extends WithNestedTestClass {
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
        .around(new NoInstanceHooksOverridesRule() {
          @Override
          protected boolean verify(Method key) {
            return !key.getName().equals("setup");
          }
        });

    @Before
    public void before() {}

    @Before
    private void privateBefore() {}

    @Before
    public void setup() {}
    
    @Test
    public void testEmpty() {}
  }

  public static class Sub1 extends Super {
    public void before() {}
  }

  public static class Sub2 extends Super {
    @Before
    public void before() {}
  }

  public static class Sub3 extends Super {
    @Before
    private void privateBefore() {}
  }

  public static class Sub4 extends Super {
    @Override
    public void setup() {}
  }

  @Test
  public void testOverrideNoAnnotation() {

    FullResult runClasses = runTests(Sub1.class);
    Assertions.assertThat(runClasses.getFailures()).isNotEmpty();
    Assertions.assertThat(runClasses.getFailures().get(0).getTrace())
    .contains("shadow or override each other");
  }
  
  @Test
  public void testOverrideWithAnnotation() {
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
  
  @Test
  public void testFiltering() {
    FullResult runClasses = runTests(Sub4.class);
    Assertions.assertThat(runClasses.getFailures()).isEmpty();
  }    
}
