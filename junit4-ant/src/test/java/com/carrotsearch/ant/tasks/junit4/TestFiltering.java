package com.carrotsearch.ant.tasks.junit4;


import org.junit.Test;


public class TestFiltering extends JUnit4XmlTestBase {
  @Test
  public void classfilter() {
    executeTarget("classfilter");
    assertLogContains("Tests summary: 1 suite");
  }
  
  @Test
  public void methodfilter() {
    executeTarget("methodfilter");
    assertLogContains("Tests summary: 1 suite, 2 tests");
  }
}
