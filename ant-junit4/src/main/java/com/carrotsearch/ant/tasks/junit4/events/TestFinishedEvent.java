package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


@SuppressWarnings("serial")
public class TestFinishedEvent extends AbstractEventWithDescription {
  public TestFinishedEvent(Description description) {
    super(EventType.TEST_FINISHED, description);
  }
}
