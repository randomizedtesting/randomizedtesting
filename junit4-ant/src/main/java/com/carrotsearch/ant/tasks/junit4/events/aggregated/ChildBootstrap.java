package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import com.carrotsearch.ant.tasks.junit4.SlaveInfo;

public class ChildBootstrap {
  public final SlaveInfo childInfo;

  public ChildBootstrap(SlaveInfo childInfo) {
    this.childInfo = childInfo;
  }
  
  public SlaveInfo getSlave() {
    return childInfo;
  }
}
