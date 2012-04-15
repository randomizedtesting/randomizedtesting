package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import com.carrotsearch.ant.tasks.junit4.tests.FailInAfterClass;


public class TestTextReport extends AntBuildFileTestBase {

  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

  @Test 
  public void suiteerror() {
    super.executeTarget("suiteerror");
    
    String output = super.getLog();
    int count = 0;
    for (int i = 0; i < output.length();) {
      int index = output.indexOf(FailInAfterClass.MESSAGE, i);
      if (index < 0) {
        break;
      }
      count++;
      i = index + 1;
    }
    
    Assert.assertEquals(1, count);
  }
}
