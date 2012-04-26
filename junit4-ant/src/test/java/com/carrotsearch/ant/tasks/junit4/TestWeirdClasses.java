package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;


public class TestWeirdClasses extends AntBuildFileTestBase {
  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

  /*
   * Just run an example that shows JUnit's behavior on package-private/ abstract classes.
   * https://github.com/carrotsearch/randomizedtesting/issues/91
   */
  @Test 
  public void antxml() {
    super.executeTarget("weirdclasses");
  }
}
