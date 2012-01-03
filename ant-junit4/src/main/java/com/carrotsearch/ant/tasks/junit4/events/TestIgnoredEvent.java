package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;

public class TestIgnoredEvent extends AbstractEventWithDescription {
  private long startTimestamp;

  protected TestIgnoredEvent() {
    super(EventType.TEST_IGNORED);
  }
  
  public TestIgnoredEvent(Description description) {
    this();
    setDescription(description);
    // For ignored tests, take the current time as the execution timestamp.
    this.startTimestamp = System.currentTimeMillis();
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }
}
