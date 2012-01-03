package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


public class TestFinishedEvent extends AbstractEventWithDescription {
  private int executionTime;
  private long startTimestamp;

  protected TestFinishedEvent() {
    super(EventType.TEST_FINISHED);
  }

  public TestFinishedEvent(Description description, int timeMillis, long startTimestamp) {
    this();
    this.executionTime = timeMillis;
    this.startTimestamp = startTimestamp;
    setDescription(description);
  }

  public int getExecutionTime() {
    return executionTime;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }
}
