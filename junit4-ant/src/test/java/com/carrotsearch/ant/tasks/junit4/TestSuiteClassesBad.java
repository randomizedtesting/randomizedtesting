package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;


public class TestSuiteClassesBad extends AntBuildFileTestBase {
  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

  @Test
  public void notaclass() {
    expectBuildExceptionContaining("notaclass",
        "File does not start with a class magic");
  }

  @Test
  public void notinstantiable() {
    expectBuildExceptionContaining("notinstantiable",
        "There were test failures: 1 suite, 1 test, 1 error");
  }
}
