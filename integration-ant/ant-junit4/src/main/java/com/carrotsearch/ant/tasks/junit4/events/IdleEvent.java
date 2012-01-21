package com.carrotsearch.ant.tasks.junit4.events;

/**
 * Marker that the slave is idle and awaiting more suite names.
 */
public class IdleEvent extends AbstractEvent {
  public IdleEvent() {
    super(EventType.IDLE);
  }
}
