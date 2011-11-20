package com.carrotsearch.ant.tasks.junit4;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * An adapter from {@link IExecutionListener} to {@link RunListener}.
 */
public final class RunListenerAdapter extends RunListener {
  private final IExecutionListener delegate;

  public RunListenerAdapter(IExecutionListener delegate) {
    this.delegate = delegate;
  }

  public void testRunStarted(Description description) throws Exception {
    delegate.testRunStarted(description);
  }

  public void testRunFinished(Result result) throws Exception {
    delegate.testRunFinished(result);
  }

  public void testStarted(Description description) throws Exception {
    delegate.testStarted(description);
  }

  public void testFinished(Description description) throws Exception {
    delegate.testFinished(description);
  }

  public void testFailure(Failure failure) throws Exception {
    delegate.testFailure(failure);
  }

  public void testAssumptionFailure(Failure failure) {
    delegate.testAssumptionFailure(failure);
  }

  public void testIgnored(Description description) throws Exception {
    delegate.testIgnored(description);
  }
}
