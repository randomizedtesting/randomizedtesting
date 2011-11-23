package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.notification.Failure;


/**
 * Serialized failure.
 */
@SuppressWarnings("serial")
public class SuiteFailureEvent extends FailureEvent {
  public SuiteFailureEvent(Failure failure) {
    super(EventType.SUITE_FAILURE, failure);
  } 
}
