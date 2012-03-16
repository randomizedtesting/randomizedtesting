package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestJsonReport extends AntBuildFileTestBase {

  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

  @Test 
  public void antxml() {
    super.executeTarget("json");

    Assert.assertTrue(
        new File(getProject().getBaseDir(), 
            "json/report.json").length() > 0);
    Assert.assertTrue(
        new File(getProject().getBaseDir(), 
            "json/report.jsonp").length() > 0);
    Assert.assertTrue(
        new File(getProject().getBaseDir(), 
            "json/output.html").length() > 0);    
  }
}
