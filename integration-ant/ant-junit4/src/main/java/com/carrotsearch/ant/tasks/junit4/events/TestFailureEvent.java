package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.notification.Failure;

public class TestFailureEvent extends FailureEvent {
  protected TestFailureEvent() {
    super(EventType.TEST_FAILURE);
  }

  public TestFailureEvent(Failure failure) {
    this();
    setFailure(failure);
  }
}
