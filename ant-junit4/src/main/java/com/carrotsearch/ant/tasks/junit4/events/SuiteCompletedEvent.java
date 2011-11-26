package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


/**
 * Serialized failure.
 */
@SuppressWarnings("serial")
public class SuiteCompletedEvent extends AbstractEvent {
  private Description description;
  private long startTimestamp;
  private long executionTime;

  public SuiteCompletedEvent(Description description, long start, long duration) {
    super(EventType.SUITE_COMPLETED);
    this.description = description;
    this.startTimestamp = start;
    this.executionTime = duration;
  }

  public Description getDescription() {
    return description;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }
}
