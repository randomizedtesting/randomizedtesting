package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;


/**
 * Serialized failure.
 */
@SuppressWarnings("serial")
public class SuiteCompletedEvent extends AbstractEvent {
  private Description description;

  public SuiteCompletedEvent(Description description) {
    super(EventType.SUITE_COMPLETED);
    this.description = description;
  }

  public Description getDescription() {
    return description;
  }
}
