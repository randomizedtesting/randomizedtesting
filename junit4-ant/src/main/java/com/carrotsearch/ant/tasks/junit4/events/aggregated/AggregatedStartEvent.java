package com.carrotsearch.ant.tasks.junit4.events.aggregated;


/**
 * An event dispatched before any forked JVM starts.
 */
public class AggregatedStartEvent {
  private int forkedJvmCount;
  private int suiteCount;

  public AggregatedStartEvent(int forkedJvmCount, int suiteCount) {
    this.forkedJvmCount = forkedJvmCount;
    this.suiteCount = suiteCount;
  }
  
  /**
   * Number of forked JVM processes.
   */
  public int getForkedJvmCount() {
    return forkedJvmCount;
  }

  /**
   * Number of test suites, total.
   */
  public int getSuiteCount() {
    return suiteCount;
  }
}
