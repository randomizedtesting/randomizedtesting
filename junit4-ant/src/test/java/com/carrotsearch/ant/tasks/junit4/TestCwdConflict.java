package com.carrotsearch.ant.tasks.junit4;


import org.junit.Test;


public class TestCwdConflict extends JUnit4XmlTestBase {
  @Test 
  public void cwdconflict() {
    super.executeTarget("cwdconflict");
  }
}
