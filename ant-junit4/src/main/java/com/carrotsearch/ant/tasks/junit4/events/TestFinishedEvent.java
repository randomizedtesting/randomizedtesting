package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


@SuppressWarnings("serial")
public class TestFinishedEvent extends AbstractEventWithDescription {
  private final int time;
  private final long startTimestamp;
  
  public TestFinishedEvent(Description description, int timeMillis, long startTimestamp) {
    super(EventType.TEST_FINISHED, description);
    this.time = timeMillis;
    this.startTimestamp = startTimestamp;
  }

  public int getExecutionTime() {
    return time;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }
}
