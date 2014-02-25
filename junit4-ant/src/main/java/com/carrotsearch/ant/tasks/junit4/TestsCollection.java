package com.carrotsearch.ant.tasks.junit4;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A collection of test suites and extracted annotation information.
 */
final class TestsCollection {
  List<TestClass> testClasses = Lists.newArrayList();

  public void add(TestClass testClass) {
    testClasses.add(testClass);
  }
}
