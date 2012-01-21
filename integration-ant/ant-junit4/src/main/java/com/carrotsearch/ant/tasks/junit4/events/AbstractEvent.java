package com.carrotsearch.ant.tasks.junit4.events;

/**
 * An abstract {@link IEvent}.
 */
abstract class AbstractEvent implements IEvent {
  /** Type is recreated in constructors anyway. */
  private transient final EventType type;

  public AbstractEvent(EventType type) {
    if (this.getClass() != type.eventClass) {
      throw new RuntimeException("Event type mismatch: "
          + type + ", class: " + this.getClass());
    }

    this.type = type;
  }
  
  @Override
  public EventType getType() {
    return type;
  }
}
