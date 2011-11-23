package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


@SuppressWarnings("serial")
public class TestStartedEvent extends AbstractEventWithDescription {
  public TestStartedEvent(Description description) {
    super(EventType.TEST_STARTED, description);
  }
}
