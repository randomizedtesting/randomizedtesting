package com.carrotsearch.ant.tasks.junit4.events.aggregated;


/**
 * An event dispatched before any slave starts.
 */
public class AggregatedStartEvent {
  private int slaves;

  public AggregatedStartEvent(int slaves) {
    this.slaves = slaves;
  }
  
  /**
   * Number of slave processes.
   */
  public int getSlaveCount() {
    return slaves;
  }
}
