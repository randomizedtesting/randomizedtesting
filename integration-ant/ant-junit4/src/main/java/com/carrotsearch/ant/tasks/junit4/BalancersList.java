package com.carrotsearch.ant.tasks.junit4;

import java.util.List;

/**
 * A nested list of {@link SuiteBalancer}s.
 */
public class BalancersList {  
  private List<SuiteBalancer> balancers;

  public BalancersList(List<SuiteBalancer> balancers) {
    this.balancers = balancers;
  }

  /**
   * Adds a balancer to the balancers list.
   */
  public void addConfigured(SuiteBalancer balancer) {
    balancers.add(balancer);
  }
}
