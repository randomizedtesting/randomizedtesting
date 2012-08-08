package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;


public class TestSysoutOOM  extends JUnit4XmlTestBase {
  @Test
  public void sysoutoom() {
    super.executeTarget("sysoutoom");
  }
}
