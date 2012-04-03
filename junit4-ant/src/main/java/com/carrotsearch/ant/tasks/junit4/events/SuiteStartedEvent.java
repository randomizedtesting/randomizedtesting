package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


/**
 * Serialized failure.
 */
public class SuiteStartedEvent extends AbstractEventWithDescription {
  protected SuiteStartedEvent() {
    super(EventType.SUITE_STARTED);
  }

  public SuiteStartedEvent(Description description) {
    this();
    setDescription(description);
  }
}