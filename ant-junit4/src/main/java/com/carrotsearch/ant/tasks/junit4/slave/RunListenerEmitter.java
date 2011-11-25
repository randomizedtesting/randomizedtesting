package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.IOException;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.ant.tasks.junit4.events.*;

/**
 * Serialize test execution events.
 */
public class RunListenerEmitter extends RunListener {
  private final Serializer serializer;
  private Description suiteDescription;
  private long start;

  public RunListenerEmitter(Serializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    this.suiteDescription = description;
    serializer.serialize(new SuiteStartedEvent(description));
  }

  @Override
  public void testStarted(Description description) throws Exception {
    serializer.serialize(new TestStartedEvent(description));
    start = System.currentTimeMillis();
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    serializer.serialize(new TestFailureEvent(failure));
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    try {
      serializer.serialize(new TestIgnoredAssumptionEvent(failure));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    serializer.serialize(new TestIgnoredEvent(description));
  }

  @Override
  public void testFinished(Description description) throws Exception {
    long executionTime = System.currentTimeMillis() - start;
    serializer.serialize(new TestFinishedEvent(description, (int) executionTime));
    serializer.flush();
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    serializer.serialize(
        new SuiteCompletedEvent(suiteDescription));
    serializer.flush();
  }  
}
