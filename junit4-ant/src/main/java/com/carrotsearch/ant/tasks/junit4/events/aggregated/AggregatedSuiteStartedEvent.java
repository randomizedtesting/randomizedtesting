package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import com.carrotsearch.ant.tasks.junit4.SlaveInfo;
import com.carrotsearch.ant.tasks.junit4.events.SuiteStartedEvent;

public class AggregatedSuiteStartedEvent {
  private transient final SlaveInfo slave;
  private SuiteStartedEvent suiteStartedEvent;

  public AggregatedSuiteStartedEvent(SlaveInfo id, SuiteStartedEvent e) {
    this.slave = id;
    this.suiteStartedEvent = e;
  }
  
  public SuiteStartedEvent getSuiteStartedEvent() {
    return suiteStartedEvent;
  }
  
  public SlaveInfo getSlave() {
    return slave;
  }
}
