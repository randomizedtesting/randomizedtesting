package com.carrotsearch.ant.tasks.junit4;


import java.io.File;

import org.junit.Assert;
import org.junit.Test;


public class TestAntXmlReport  extends JUnit4XmlTestBase {

  @Test 
  public void antxml() {
    super.executeTarget("antxml");

    Assert.assertTrue(
        new File(getProject().getBaseDir(), "ant-xmls/TEST-com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError.xml").length() > 0);
  }
}
