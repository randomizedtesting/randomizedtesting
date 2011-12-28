package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


@SuppressWarnings("serial")
public class TestIgnoredEvent extends AbstractEventWithDescription {
  private final long startTimestamp;
  
  public TestIgnoredEvent(Description description) {
    super(EventType.TEST_IGNORED, description);

    // For ignored tests, take the current time as the execution timestamp.
    this.startTimestamp = System.currentTimeMillis();
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }
}
