package com.carrotsearch.ant.tasks.junit4.events;

@SuppressWarnings("serial")
abstract class AbstractEvent implements IEvent {
  private final EventType type;

  public AbstractEvent(EventType type) {
    if (this.getClass() != type.eventClass) {
      throw new RuntimeException("Unmatched event type: "
          + type + ", class: " + this.getClass());
    }

    this.type = type;
  }
  
  @Override
  public EventType getType() {
    return type;
  }
}
