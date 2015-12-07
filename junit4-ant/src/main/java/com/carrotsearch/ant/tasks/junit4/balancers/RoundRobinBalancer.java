package com.carrotsearch.ant.tasks.junit4.balancers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.SuiteBalancer;

/**
 * A round-robin suite balancer (default for non-assigned suites).
 */
public class RoundRobinBalancer implements SuiteBalancer {
 
  @Override
  public List<Assignment> assign(Collection<String> suiteNames, int slaves, long seed) {
    List<Assignment> result = new ArrayList<>();
    int i = 0;
    for (String suite : suiteNames) {
      result.add(new Assignment(suite, i++, 0));
      if (i >= slaves) i = 0;
    }
    return result;
  }

  @Override
  public void setOwner(JUnit4 owner) {}
}
