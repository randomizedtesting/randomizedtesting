package com.carrotsearch.randomizedtesting.rules;

import org.fest.assertions.api.Assertions;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.model.Statement;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;

public class TestStaticFieldsInvariantRule extends WithNestedTestClass {
  static int LEAK_THRESHOLD = 5 * 1024 * 1024;

  public static class Base extends RandomizedTest {
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
        .around(new StaticFieldsInvariantRule(LEAK_THRESHOLD, true));

    @Test
    public void testEmpty() {}
  }

  public static class Smaller extends Base {
    
    static byte [] field0; 
    
    @SuppressWarnings("unused")
    @BeforeClass
    private static void setup() {
      field0 = new byte [LEAK_THRESHOLD / 2];
    }
  }

  public static class Exceeding extends Smaller {
    static byte [] field1;
    static byte [] field2;
    static int [] field3;
    static long field4;
    final static long [] field5 = new long [1024];

    @SuppressWarnings("unused")
    @BeforeClass
    private static void setup() {
      field1 = new byte [LEAK_THRESHOLD / 2];
      field2 = new byte [100];
      field3 = new int [100];
    }
  }

  @Test
  public void testPassingUnderThreshold() {
    Result runClasses = JUnitCore.runClasses(Smaller.class);
    Assertions.assertThat(runClasses.getFailures()).isEmpty();
  }
  
  @Test
  public void testFailingAboveThreshold() {
    Result runClasses = JUnitCore.runClasses(Exceeding.class);
    Assertions.assertThat(runClasses.getFailures()).hasSize(1);
    
    Assertions.assertThat(runClasses.getFailures().get(0).getTrace())
      .contains(".field0")
      .contains(".field1")
      .contains(".field2")
      .contains(".field3")
      .doesNotContain(".field5");
  }
}
