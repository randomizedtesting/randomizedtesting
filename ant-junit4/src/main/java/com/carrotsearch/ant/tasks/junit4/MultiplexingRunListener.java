package com.carrotsearch.ant.tasks.junit4;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * A {@Link RunListener} that multiplexes events to a collection of other
 * {@link RunListener}.
 */
class MultiplexingRunListener extends RunListener {
  private RunListener[] listeners;

  public MultiplexingRunListener(List<RunListener> listeners) {
    for (RunListener r : listeners) {
      if (r == null) 
        throw new IllegalArgumentException("No null listeners allowed.");
    }

    this.listeners = listeners.toArray(new RunListener [listeners.size()]);
  }

  @Override
  public void testRunStarted(Description suiteDescription) throws Exception {
    for (RunListener listener : listeners) {
      try {
        listener.testRunStarted(suiteDescription);
      } catch (Throwable t) {
        logError(t);
      }
    }
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    for (RunListener listener : listeners) {
      try {
        listener.testRunFinished(result);
      } catch (Throwable t) {
        logError(t);
      }
    }
  }

  @Override
  public void testStarted(Description testDescription) throws Exception {
    for (RunListener listener : listeners) {
      try {
        listener.testStarted(testDescription);
      } catch (Throwable t) {
        logError(t);
      }
    }
  }

  @Override
  public void testFinished(Description testDescription) throws Exception {
    for (RunListener listener : listeners) {
      try {
        listener.testFinished(testDescription);
      } catch (Throwable t) {
        logError(t);
      }
    }
  }

  @Override
  public void testIgnored(Description testDescription) throws Exception {
    for (RunListener listener : listeners) {
      try {
        listener.testIgnored(testDescription);
      } catch (Throwable t) {
        logError(t);
      }
    }
  }

  @Override
  public void testFailure(Failure testDescription) throws Exception {
    for (RunListener listener : listeners) {
      try {
        listener.testFailure(testDescription);
      } catch (Throwable t) {
        logError(t);
      }
    }
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    for (RunListener listener : listeners) {
      try {
        listener.testAssumptionFailure(failure);
      } catch (Throwable t) {
        logError(t);
      }
    }
  }

  private void logError(Throwable t) {
    Logger.getAnonymousLogger().log(Level.SEVERE, "Error dispatching RunListener method.", t);
  }
}
