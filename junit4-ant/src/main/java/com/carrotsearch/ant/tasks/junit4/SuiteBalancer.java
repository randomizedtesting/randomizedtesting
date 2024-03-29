package com.carrotsearch.ant.tasks.junit4;

import java.util.Collection;
import java.util.List;

/**
 * A test balancer schedules test suites to be executed on a given JVM.
 */
public interface SuiteBalancer {
  public final static class Assignment implements Comparable<Assignment> {
    /**
     * Test suite name.
     */
    public final String suiteName;

    /**
     * forked JVM assignment.
     */
    public final int forkedJvmId;
    
    /**
     * Estimated cost; informational only (depends on the balancer). May be zero
     * for balancers which don't use cost at all.
     */
    public final int estimatedCost;

    public Assignment(String suiteName, int forkedJvmId, int estimatedCost) {
      this.suiteName = suiteName;
      this.forkedJvmId = forkedJvmId;
      this.estimatedCost = estimatedCost;
    }

    @Override
    public int compareTo(Assignment other) {
      int v = this.suiteName.compareTo(other.suiteName);
      if (v == 0) {
        v = this.forkedJvmId - other.forkedJvmId;
      }
      return v;
    }
  }

  /**
   * Sets the owner task (for logging mostly).
   */
  void setOwner(JUnit4 owner);
  
  /**
   * Provide assignments for suite names and a given number of forked JVMs.
   * 
   * @return Returns an ordered list with assignments. Any suite name not present
   *         in the keys of the returned map will be assigned by following
   *         balancers (or randomly).
   */
  List<Assignment> assign(Collection<String> suiteNames, int forkedJvmCount, long seed);
}
