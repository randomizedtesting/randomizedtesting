package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.notification.Failure;

@SuppressWarnings("serial")
public class TestIgnoredAssumptionEvent extends FailureEvent {
  public TestIgnoredAssumptionEvent(Failure failure) {
    super(EventType.TEST_IGNORED_ASSUMPTION, failure);
  }
}
