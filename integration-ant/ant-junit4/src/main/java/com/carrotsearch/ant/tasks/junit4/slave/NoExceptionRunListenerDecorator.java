package com.carrotsearch.ant.tasks.junit4.slave;

import java.lang.reflect.Proxy;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * {@link RunListener} decorator that does something before and after a given method call.
 * A fancier impl. could use a {@link Proxy} but {@link RunListener} is not an interface.
 */
public abstract class NoExceptionRunListenerDecorator extends RunListener {
  private final RunListener delegate;
  
  public NoExceptionRunListenerDecorator(RunListener delegate) {
    this.delegate = delegate;
  }

  public final void testRunStarted(Description description) throws Exception {
    try {
      delegate.testRunStarted(description);
    } catch (Throwable t) {
      exception(t);
    }
  }

  public final void testRunFinished(Result result) throws Exception {
    try {
      delegate.testRunFinished(result);
    } catch (Throwable t) {
      exception(t);
    }
  }

  public final void testStarted(Description description) throws Exception {
    try {
      delegate.testStarted(description);
    } catch (Throwable t) {
      exception(t);
    }
  }

  public final void testFinished(Description description) throws Exception {
    try {
      delegate.testFinished(description);
    } catch (Throwable t) {
      exception(t);
    }
  }

  public final void testFailure(Failure failure) throws Exception {
    try {
      delegate.testFailure(failure);
    } catch (Throwable t) {
      exception(t);
    }
  }

  public final void testAssumptionFailure(Failure failure) {
    try {
      delegate.testAssumptionFailure(failure);
    } catch (Throwable t) {
      exception(t);
    }
  }

  public final void testIgnored(Description description) throws Exception {
    try {
      delegate.testIgnored(description);
    } catch (Throwable t) {
      exception(t);
    }
  }

  protected abstract void exception(Throwable t);
}
