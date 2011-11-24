package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


@SuppressWarnings("serial")
public class TestFinishedEvent extends AbstractEventWithDescription {
  private final int time;
  
  public TestFinishedEvent(Description description, int timeMillis) {
    super(EventType.TEST_FINISHED, description);
    this.time = timeMillis;
  }

  public int getExecutionTime() {
    return time;
  }
}
