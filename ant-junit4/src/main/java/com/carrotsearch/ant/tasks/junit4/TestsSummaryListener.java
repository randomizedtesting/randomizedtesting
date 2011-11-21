package com.carrotsearch.ant.tasks.junit4;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * Aggregate multiple concurrent executions into a single stream of events.
 */
public class TestsSummaryListener extends ExecutionListenerAdapter {
  private int failures;
  private int tests;
  private int errors;
  private int assumptions;
  private int ignores;

  @Override
  public void testStarted(Description description) throws Exception {
    tests++;
  }
  
  @Override
  public void testFailure(Failure failure) throws Exception {
    if (failure.getException() instanceof AssertionError) {
      failures++;
    } else {
      errors++;
    }
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    assumptions++;
  }
  
  @Override
  public void testIgnored(Description description) throws Exception {
    ignores++;
  }
  
  /**
   * Return the summary of all tests.
   */
  public TestsSummary getResult() {
    return new TestsSummary(tests, failures, errors, assumptions, ignores);
  }
}
