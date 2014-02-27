package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;


public class TestReplication extends JUnit4XmlTestBase {
  @Test
  public void singleSuiteReplication() {
    super.executeTarget("replicateSingleTest");

    assertLogContains("Replicated test, VM: 0");
    assertLogContains("Replicated test, VM: 1");
    assertLogContains("Replicated test, VM: 2");
  }
}
