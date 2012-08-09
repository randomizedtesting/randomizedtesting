package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


/**
 * Serialized failure.
 */
public class SuiteStartedEvent extends AbstractEventWithDescription {
  private long startTimestamp;

  protected SuiteStartedEvent() {
    super(EventType.SUITE_STARTED);
  }

  public SuiteStartedEvent(Description description, long startTimestamp) {
    this();
    setDescription(description);
    this.startTimestamp = startTimestamp;
  }
  
  public long getStartTimestamp() {
    return startTimestamp;
  }
}