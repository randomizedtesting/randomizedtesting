package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;

public class TestStartedEvent extends AbstractEventWithDescription {
  protected TestStartedEvent() {
    super(EventType.TEST_STARTED);
  }

  public TestStartedEvent(Description description) {
    this();
    setDescription(description);
  }
}
