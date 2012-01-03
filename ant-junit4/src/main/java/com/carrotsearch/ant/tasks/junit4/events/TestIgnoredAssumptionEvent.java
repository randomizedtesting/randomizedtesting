package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.notification.Failure;

public class TestIgnoredAssumptionEvent extends FailureEvent {
  protected TestIgnoredAssumptionEvent() {
    super(EventType.TEST_IGNORED_ASSUMPTION);
  }

  public TestIgnoredAssumptionEvent(Failure failure) {
    this();
    setFailure(failure);
  }
}
