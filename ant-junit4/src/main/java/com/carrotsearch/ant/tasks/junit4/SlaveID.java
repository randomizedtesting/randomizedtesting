package com.carrotsearch.ant.tasks.junit4;

/**
 * Static slave information.
 */
public final class SlaveID {
  /**
   * Unique sequential slave identifier.
   */
  public final int id;

  /**
   * The number of executed slaves, total.
   */
  public final int slaves;

  /* */
  public SlaveID(int id, int slaves) {
    this.id = id;
    this.slaves = slaves;
  }
}
