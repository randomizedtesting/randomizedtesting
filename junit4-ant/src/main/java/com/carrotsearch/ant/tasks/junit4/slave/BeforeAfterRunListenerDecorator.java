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
public abstract class BeforeAfterRunListenerDecorator extends RunListener {
  private final RunListener delegate;

  public BeforeAfterRunListenerDecorator(RunListener delegate) {
    this.delegate = delegate;
  }

  public final void testRunStarted(Description description) throws Exception {
    before();
    try {
      delegate.testRunStarted(description);
    } finally {
      after();
    }
  }

  public final void testRunFinished(Result result) throws Exception {
    before();
    try {
      delegate.testRunFinished(result);
    } finally {
      after();
    }
  }

  public final void testStarted(Description description) throws Exception {
    before();
    try {
      delegate.testStarted(description);
    } finally {
      after();
    }
  }

  public final void testFinished(Description description) throws Exception {
    before();
    try {
      delegate.testFinished(description);
    } finally {
      after();
    }
  }

  public final void testFailure(Failure failure) throws Exception {
    before();
    try {
      delegate.testFailure(failure);
    } finally {
      after();
    }
  }

  public final void testAssumptionFailure(Failure failure) {
    before();
    try {
      delegate.testAssumptionFailure(failure);
    } finally {
      after();
    }
  }

  public final void testIgnored(Description description) throws Exception {
    before();
    try {
      delegate.testIgnored(description);
    } finally {
      after();
    }
  }

  protected void after() {}
  protected void before() {}  
}
