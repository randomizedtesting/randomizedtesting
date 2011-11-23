package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


@SuppressWarnings("serial")
public class TestIgnoredEvent extends AbstractEventWithDescription {
  public TestIgnoredEvent(Description description) {
    super(EventType.TEST_IGNORED, description);
  }
}
