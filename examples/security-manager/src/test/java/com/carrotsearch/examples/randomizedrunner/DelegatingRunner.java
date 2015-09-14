package com.carrotsearch.examples.randomizedrunner;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/** Pollutes the stack with non-RR code, so we know the AccessController
    blocks are correct. */
public class DelegatingRunner extends Runner implements Filterable {
  private final RandomizedRunner delegate;
  
  public DelegatingRunner(Class<?> testClass) throws InitializationError {
    delegate = new RandomizedRunner(testClass);
  }

  @Override
  public int testCount() {
    return delegate.testCount();
  }

  @Override
  public void filter(Filter filter) throws NoTestsRemainException {
    delegate.filter(filter);    
  }

  @Override
  public Description getDescription() {
    return delegate.getDescription();
  }

  @Override
  public void run(RunNotifier notifier) {
    delegate.run(notifier);
  }
}
