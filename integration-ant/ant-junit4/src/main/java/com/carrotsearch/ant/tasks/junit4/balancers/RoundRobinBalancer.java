package com.carrotsearch.ant.tasks.junit4.balancers;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.TestBalancer;
import com.google.common.collect.Maps;

/**
 * A round-robin suite balancer (default for non-assigned suites).
 */
public class RoundRobinBalancer implements TestBalancer {
  @Override
  public LinkedHashMap<String,Assignment> assign(Collection<String> suiteNames, int slaves, long seed) {
    LinkedHashMap<String,Assignment> result = Maps.newLinkedHashMap();
    int i = 0;
    for (String suite : suiteNames) {
      result.put(suite, new Assignment(i++, 0));
      if (i >= slaves) i = 0;
    }
    return result;
  }

  @Override
  public void setOwner(JUnit4 owner) {}
}
