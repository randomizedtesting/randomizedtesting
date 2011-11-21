package com.carrotsearch.ant.tasks.junit4;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Empty implementation of {@link IExecutionListener}.
 */
public class ExecutionListenerAdapter implements IExecutionListener {
  @Override
  public void testRunStarted(Description description) throws Exception {}

  @Override
  public void testRunFinished(Result result) throws Exception {}

  @Override
  public void testStarted(Description description) throws Exception {}

  @Override
  public void testFailure(Failure failure) throws Exception {}

  @Override
  public void testAssumptionFailure(Failure failure) {}

  @Override
  public void testIgnored(Description description) throws Exception {}

  @Override
  public void testFinished(Description description) throws Exception {}

  @Override
  public void appendOut(byte[] outputBytes) {}

  @Override
  public void appendErr(byte[] outputBytes) {} 
}
