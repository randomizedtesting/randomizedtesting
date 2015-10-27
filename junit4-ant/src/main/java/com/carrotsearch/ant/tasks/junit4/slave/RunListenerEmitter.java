package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.ant.tasks.junit4.events.*;

/**
 * Serialize test execution events. Attempts to handle
 * certain corner cases that are not cleanly handled by
 * JUnit itself (reporting the cause of ignored methods, 
 * etc.).
 */
public class RunListenerEmitter extends RunListener {
  private final Serializer serializer;
  private Description suiteDescription;
  private long start;
  private long suiteStart;

  /** 
   * A failure signaled at the suite level. Most likely will result
   * in ignored methods.
   */
  private Failure suiteAssumption;

  public RunListenerEmitter(Serializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    this.suiteDescription = description;
    this.suiteStart = System.currentTimeMillis();
    this.suiteAssumption = null;
    serializer.serialize(new SuiteStartedEvent(description, suiteStart));
  }

  @Override
  public void testStarted(Description description) throws Exception {
    serializer.serialize(new TestStartedEvent(description));
    start = System.currentTimeMillis();
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    if (suiteDescription.equals(failure.getDescription())) {
      serializer.serialize(new SuiteFailureEvent(failure));
    } else {
      serializer.serialize(new TestFailureEvent(failure));
    }
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    try {
      // Check for class-level assumption failures that may result
      // in message-less ignored tests. See GH-103. 
      if (suiteDescription != null && 
          suiteDescription.equals(failure.getDescription())) {
        suiteAssumption = failure;
      }

      serializer.serialize(new TestIgnoredAssumptionEvent(failure));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    if (suiteDescription.equals(description)) {
      // JUnitCore has built-in precedence of runners and handles @Ignore
      // with a different runner, regardless of what is declared in @RunWith. This is
      // bad.
    } else {
      // GH-82: try to determine the reason why the test has been ignored and populate
      // the cause message. This really should be passed from the runner but the API does
      // not allow it.
      String cause = "Unknown reason for ignore status.";
      
      // GH-103: check suite-level assumptions.
      if (suiteAssumption != null) {
        String msg = suiteAssumption.getMessage();
        cause = (msg != null ? msg : "Class assumption-ignored.");
      }

      try {
        Ignore ignoreAnn = description.getAnnotation(Ignore.class);
        if ((ignoreAnn = description.getAnnotation(Ignore.class)) != null) {
          cause = "Annotated @Ignore(" + ignoreAnn.value() + ")"; 
        } else {
          // Try class.
          ignoreAnn = description.getTestClass().getAnnotation(Ignore.class);
          if (ignoreAnn != null) {
            cause = "Class annotated @Ignore(" + ignoreAnn.value() + ")";
          }
        }
      } catch (Throwable t) {
        // Never fail on this.
      }
      serializer.serialize(new TestIgnoredEvent(description, cause));
    }
  }

  @Override
  public void testFinished(Description description) throws Exception {
    long executionTime = System.currentTimeMillis() - start;
    serializer.serialize(new TestFinishedEvent(description, executionTime, start));
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    suiteAssumption = null;
    final long duration = System.currentTimeMillis() - suiteStart;
    serializer.serialize(
        new SuiteCompletedEvent(suiteDescription, suiteStart, duration));
  }  
}
