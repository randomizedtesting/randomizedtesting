package com.carrotsearch.ant.tasks.junit4.events.aggregated;


/**
 * An event dispatched before any slave starts.
 */
public class AggregatedStartEvent {
  private int slaves;
  private int suiteCount;

  public AggregatedStartEvent(int slaves, int suiteCount) {
    this.slaves = slaves;
    this.suiteCount = suiteCount;
  }
  
  /**
   * Number of slave processes.
   */
  public int getSlaveCount() {
    return slaves;
  }

  /**
   * Number of test suites, total.
   */
  public int getSuiteCount() {
    return suiteCount;
  }
}
