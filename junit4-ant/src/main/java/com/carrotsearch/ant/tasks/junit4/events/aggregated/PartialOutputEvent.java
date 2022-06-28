package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.IStreamEvent;

/**
 * Partial output emitted from the forked JVM.
 */
public class PartialOutputEvent {
  private ForkedJvmInfo forkedJvmInfo;
  private IEvent event;

  public PartialOutputEvent(ForkedJvmInfo forkedJvmInfo, IEvent e) {
    assert e instanceof IStreamEvent;
    this.forkedJvmInfo = forkedJvmInfo;
    this.event = e;
  }
  
  public ForkedJvmInfo getForkedJvmInfo() {
    return forkedJvmInfo;
  }
  
  public IEvent getEvent() {
    return event;
  }
}
