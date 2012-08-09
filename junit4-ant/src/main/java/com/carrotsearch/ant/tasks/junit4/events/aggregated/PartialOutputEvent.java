package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import com.carrotsearch.ant.tasks.junit4.SlaveInfo;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;

/**
 * Partial output emitted from a given slave.
 */
public class PartialOutputEvent {
  private SlaveInfo slave;
  private IEvent event;

  public PartialOutputEvent(SlaveInfo slave, IEvent e) {
    assert e instanceof IStreamEvent;
    this.slave = slave;
    this.event = e;
  }
  
  public SlaveInfo getSlave() {
    return slave;
  }
  
  public IEvent getEvent() {
    return event;
  }
}
