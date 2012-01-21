package com.carrotsearch.ant.tasks.junit4;

import java.util.List;

/**
 * A nested list of {@link TestBalancer}s.
 */
public class BalancersList {  
  private List<TestBalancer> balancers;

  public BalancersList(List<TestBalancer> balancers) {
    this.balancers = balancers;
  }

  /**
   * Adds a balancer to the balancers list.
   */
  public void addConfigured(TestBalancer balancer) {
    balancers.add(balancer);
  }
}
