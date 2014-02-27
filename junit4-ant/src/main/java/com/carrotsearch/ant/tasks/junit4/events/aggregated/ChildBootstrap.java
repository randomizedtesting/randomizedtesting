package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;

public class ChildBootstrap {
  public final ForkedJvmInfo childInfo;

  public ChildBootstrap(ForkedJvmInfo childInfo) {
    this.childInfo = childInfo;
  }
  
  public ForkedJvmInfo getSlave() {
    return childInfo;
  }
}
