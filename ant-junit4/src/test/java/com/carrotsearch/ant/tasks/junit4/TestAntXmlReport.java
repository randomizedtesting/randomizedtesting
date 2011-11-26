package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestAntXmlReport extends AntBuildFileTestBase {

  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

  @Test 
  public void antxml() {
    super.executeTarget("antxml");

    Assert.assertTrue(
        new File(getProject().getBaseDir(), "ant-xmls/TEST-com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError.xml").length() > 0);
  }
}
