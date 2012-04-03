package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;


public class TestJUnitCompat extends AntBuildFileTestBase {

  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

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
