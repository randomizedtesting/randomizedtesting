package com.carrotsearch.ant.tasks.junit4;


import org.junit.Test;


public class TestFileEncodings extends JUnit4XmlTestBase {
  @Test
  public void checkTypicalEncodings() {
    super.executeTarget("fileencodings");
  }
}
