package com.carrotsearch.ant.tasks.junit4;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A collection of test suites and extracted annotation information.
 */
final class TestsCollection {
  List<TestClass> testClasses = Lists.newArrayList();

  public void add(TestClass testClass) {
    testClasses.add(testClass);
  }

  public void onlyUniqueSuiteNames() {
    Map<String, TestClass> unique = Maps.newLinkedHashMap();
    for (TestClass t : testClasses) {
      unique.put(t.className, t);
    }
    testClasses.clear();
    testClasses.addAll(unique.values());
  }

  public boolean hasReplicatedSuites() {
    for (TestClass t : testClasses) {
      if (t.replicate) return true;
    }
    return false;
  }
}
