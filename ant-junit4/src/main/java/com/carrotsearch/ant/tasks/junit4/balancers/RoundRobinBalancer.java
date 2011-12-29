package com.carrotsearch.ant.tasks.junit4.balancers;

import java.util.Collection;
import java.util.Map;

import com.carrotsearch.ant.tasks.junit4.TestBalancer;
import com.google.common.collect.Maps;

/**
 * A round-robin suite balancer (default for non-assigned suites).
 */
public class RoundRobinBalancer implements TestBalancer {
  @Override
  public Map<String,Integer> assign(Collection<String> suiteNames, int slaves, long seed) {
    Map<String,Integer> result = Maps.newHashMap();
    int i = 0;
    for (String suite : suiteNames) {
      result.put(suite, i++);
      if (i >= slaves) i = 0;
    }
    return result;
  }
}
