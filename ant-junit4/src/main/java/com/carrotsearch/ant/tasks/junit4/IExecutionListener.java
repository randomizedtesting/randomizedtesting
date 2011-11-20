package com.carrotsearch.ant.tasks.junit4;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Base tests execution progress tracking interface extracted from {@link RunListener}.
 */
public interface IExecutionListener {
  public void testRunStarted(Description description) throws Exception;
  public void testRunFinished(Result result) throws Exception;

  public void testStarted(Description description) throws Exception;
  public void testFailure(Failure failure) throws Exception;
  public void testAssumptionFailure(Failure failure);
  public void testIgnored(Description description) throws Exception;
  public void testFinished(Description description) throws Exception;

  public void appendOut(byte [] outputBytes);
  public void appendErr(byte [] outputBytes);
}
