package com.carrotsearch.ant.tasks.junit4.events;

import org.junit.runner.Description;

abstract class AbstractEventWithDescription extends AbstractEvent implements IDescribable {
  private Description description;

  public AbstractEventWithDescription(EventType type) {
    super(type);
  }

  public Description getDescription() {
    return description;
  }
  
  protected void setDescription(Description description) {
    if (this.description != null)
      throw new IllegalStateException("Initialize once.");
    this.description = description;
  }  
}
