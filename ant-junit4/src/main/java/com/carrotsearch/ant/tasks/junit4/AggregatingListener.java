package com.carrotsearch.ant.tasks.junit4;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Aggregate multiple concurrent executions into a single stream of events.
 */
public class AggregatingListener extends ExecutionListenerAdapter {
  private int failures;
  private int tests;
  private int assumptionIgnored;
  private int ignored;

  @Override
  public void testRunStarted(Description description) throws Exception {}
  
  @Override
  public void testRunFinished(Result result) throws Exception {}
  
  @Override
  public void testStarted(Description description) throws Exception {
    System.out.println(
        description.getClassName() + "#" + description.getMethodName());
    tests++;
  }
  
  @Override
  public void testFailure(Failure failure) throws Exception {
    failures++;
  }
  
  @Override
  public void testAssumptionFailure(Failure failure) {
    assumptionIgnored++;
  }
  
  @Override
  public void testIgnored(Description description) throws Exception {
    ignored++;
  }
  
  @Override
  public void testFinished(Description description) throws Exception {
  }
  
  @Override
  public void appendOut(byte[] outputBytes) {}
  
  @Override
  public void appendErr(byte[] outputBytes) {}
}
