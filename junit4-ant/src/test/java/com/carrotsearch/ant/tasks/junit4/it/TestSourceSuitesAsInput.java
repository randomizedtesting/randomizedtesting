package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;

public class TestSourceSuitesAsInput extends JUnit4XmlTestBase {
  @Test 
  public void sourcesuites() {
    super.executeTarget("sourcesuites");
    assertLogContains("Tests summary: 1 suite, 1 test");
  }
}
