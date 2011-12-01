package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;


public class TestFiltering extends AntBuildFileTestBase {

  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

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
