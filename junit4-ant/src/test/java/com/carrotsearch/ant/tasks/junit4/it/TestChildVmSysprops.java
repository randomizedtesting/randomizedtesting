package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;


public class TestChildVmSysprops extends JUnit4XmlTestBase {
  @Test
  public void sysprops() {
    executeTarget("childvm_sysprops");
  }
}
