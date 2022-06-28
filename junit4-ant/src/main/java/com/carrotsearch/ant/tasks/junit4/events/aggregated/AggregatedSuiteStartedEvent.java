package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.events.SuiteStartedEvent;

public class AggregatedSuiteStartedEvent {
  private transient final ForkedJvmInfo forkedJvmInfo;
  private SuiteStartedEvent suiteStartedEvent;

  public AggregatedSuiteStartedEvent(ForkedJvmInfo id, SuiteStartedEvent e) {
    this.forkedJvmInfo = id;
    this.suiteStartedEvent = e;
  }
  
  public SuiteStartedEvent getSuiteStartedEvent() {
    return suiteStartedEvent;
  }
  
  public ForkedJvmInfo getForkedJvmInfo() {
    return forkedJvmInfo;
  }
}
