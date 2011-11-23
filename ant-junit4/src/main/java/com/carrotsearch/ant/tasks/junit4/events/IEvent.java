package com.carrotsearch.ant.tasks.junit4.events;

import java.io.Serializable;

/**
 * An event/ message passed between the slave and the master. This allows for
 * some flexibility in adding new events and keeping old code functional/
 * compatible.
 * 
 * <p>We'll use built-in Java serialization for simplicity. We will only serialize
 * standard types anyway and the event stream shouldn't be that large to worry about
 * the throughput.
 */
public interface IEvent extends Serializable {
  EventType getType();
}
