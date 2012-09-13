package com.carrotsearch.ant.tasks.junit4.it;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;

public class TestAntXmlReport  extends JUnit4XmlTestBase {
  @Test 
  public void antxml() throws Exception {
    super.executeTarget("antxml");

    // Simple check for existence.
    Assert.assertTrue(
        new File(getProject().getBaseDir(), "ant-xmls/TEST-com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError.xml").length() > 0);

    // Attempt to read and parse.
    File basedir = new File(getProject().getBaseDir(), "ant-xmls");
    for (File f : basedir.listFiles()) {
      if (f.isFile() && f.getName().endsWith(".xml")) {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        docBuilder.parse(f);
      }
    }
  }
}
