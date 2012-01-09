package com.carrotsearch.examples.randomizedrunner.reports;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * Multiple failures from a single test case (followed by a suite-failure).
 */
public class Test003MultipleFailures {
  @Rule
  public TestRule rule = new TestRule() {
    @Override
    public Statement apply(final Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          try {
            base.evaluate();
          } catch (Throwable t) {
            throw new MultipleFailureException(Arrays.<Throwable>asList(
                t,
                new Exception("a"),
                new Exception("b")));
          }
        }
      };
    }
  };

  @Test
  public void testCase() {
    throw new RuntimeException("testCase");
  }

  @After
  public void after() {
    throw new RuntimeException("after");
  }

  @AfterClass
  public static void afterClass() {
    throw new RuntimeException("afterClass");
  }
}
