package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


/**
 * Serialized failure.
 */
public class SuiteCompletedEvent extends AbstractEventWithDescription {
  private long startTimestamp;
  private long executionTime;

  protected SuiteCompletedEvent() {
    super(EventType.SUITE_COMPLETED);
  }

  public SuiteCompletedEvent(Description description, long start, long duration) {
    this();
    this.startTimestamp = start;
    this.executionTime = duration;
    setDescription(description);
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }
}
