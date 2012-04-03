package com.carrotsearch.ant.tasks.junit4.events;


/**
 * An event/ message passed between the slave and the master.
 */
public interface IEvent {
  EventType getType();
}
