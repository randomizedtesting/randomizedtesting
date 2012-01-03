package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.notification.Failure;

/**
 * Serialized failure.
 */
public class SuiteFailureEvent extends FailureEvent {
  protected SuiteFailureEvent() {
    super(EventType.SUITE_FAILURE);
  }

  public SuiteFailureEvent(Failure failure) {
    this();
    setFailure(failure);
  } 
}
