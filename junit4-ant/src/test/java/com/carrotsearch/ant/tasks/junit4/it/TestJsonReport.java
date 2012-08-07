package com.carrotsearch.ant.tasks.junit4.it;


import java.io.File;

import org.junit.Assert;
import org.junit.Test;


public class TestJsonReport extends JUnit4XmlTestBase {
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
