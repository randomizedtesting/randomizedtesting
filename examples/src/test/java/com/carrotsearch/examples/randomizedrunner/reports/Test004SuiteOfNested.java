package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * A suite of nested test classes.
 */
@RunWith(Suite.class)
@SuiteClasses({
  Test004SuiteOfNested.Subclass1.class,
  Test004SuiteOfNested.Subclass2.class,
  Test004SuiteOfNested.Subclass3.class
})
public class Test004SuiteOfNested {
  public static class Subclass1 extends Test001AllStatuses {
  }

  public static class Subclass2 extends Subclass1 {
  }

  public static class Subclass3 extends Subclass1 {
  }
}
