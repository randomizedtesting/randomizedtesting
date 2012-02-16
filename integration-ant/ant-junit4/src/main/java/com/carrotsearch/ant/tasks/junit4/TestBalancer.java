package com.carrotsearch.ant.tasks.junit4;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A test balancer schedules test suites to be executed on a given JVM.
 */
public interface TestBalancer {
  public final static class Assignment {
    /**
     * Slave assignment.
     */
    public final int slaveId;
    
    /**
     * Estimated cost; informational only (depends on the balancer). May be zero
     * for balancers which don't use cost at all.
     */
    public final int estimatedCost;
    
    public Assignment(int slaveId, int estimatedCost) {
      this.slaveId = slaveId;
      this.estimatedCost = estimatedCost;
    }
  }
  
  /**
   * Sets the owner task (for logging mostly).
   */
  void setOwner(JUnit4 owner);
  
  /**
   * Provide assignments for suite names and a given number of slaves.
   * 
   * @return Returns an ordered map with assignments. Any suite name not present
   *         in the keys of the returned map will be assigned by following
   *         balancers (or randomly).
   */
  LinkedHashMap<String,Assignment> assign(Collection<String> suiteNames, int slaves, long seed);
}
