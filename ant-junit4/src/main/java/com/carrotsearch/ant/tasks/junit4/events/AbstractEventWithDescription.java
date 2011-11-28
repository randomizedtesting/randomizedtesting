package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;

@SuppressWarnings("serial")
public abstract class AbstractEventWithDescription extends AbstractEvent {
  private final Description description;

  public AbstractEventWithDescription(EventType type, Description description) {
    super(type);
    this.description = description;
  }

  public Description getDescription() {
    return description;
  }
}
