package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;

public class TestWeirdClasses  extends JUnit4XmlTestBase {
  /*
   * Just run an example that shows JUnit's behavior on package-private/ abstract classes.
   * https://github.com/carrotsearch/randomizedtesting/issues/91
   */
  @Test 
  public void antxml() {
    super.executeTarget("weirdclasses");
  }
}
