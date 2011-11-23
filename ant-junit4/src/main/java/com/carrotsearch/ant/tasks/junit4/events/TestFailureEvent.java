package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.notification.Failure;


@SuppressWarnings("serial")
public class TestFailureEvent extends FailureEvent {
  public TestFailureEvent(Failure failure) {
    super(EventType.TEST_FAILURE, failure);
  }
}
