package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


/**
 * Serialized failure.
 */
@SuppressWarnings("serial")
public class SuiteStartedEvent extends AbstractEvent {
  private Description description;

  public SuiteStartedEvent(Description description) {
    super(EventType.SUITE_STARTED);
    this.description = description;
  }

  public Description getDescription() {
    return description;
  }
}
