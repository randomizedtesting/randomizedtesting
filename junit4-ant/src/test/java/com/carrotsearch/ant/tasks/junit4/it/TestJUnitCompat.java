package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;


public class TestJUnitCompat extends JUnit4XmlTestBase {
  @Test
  public void junitcompat1() {
      super.expectBuildExceptionContaining(
          "junitcompat1", "<formatter> elements are not supported");
  }
  
  @Test
  public void junitcompat2() {
      super.expectBuildExceptionContaining(
          "junitcompat2", "<test> elements are not supported");
  }

  @Test
  public void junitcompat3() {
      super.expectBuildExceptionContaining(
          "junitcompat3", "<batchtest> elements are not supported");
  }
}
