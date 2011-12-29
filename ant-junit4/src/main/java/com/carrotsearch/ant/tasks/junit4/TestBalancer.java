package com.carrotsearch.ant.tasks.junit4;

import java.util.Collection;
import java.util.Map;

/**
 * A test balancer schedules test suites to be executed on a given JVM.
 */
public interface TestBalancer {
  Map<String,Integer> assign(Collection<String> suiteNames, int slaves, long seed);
}
