package com.carrotsearch.ant.tasks.junit4.it;

import static org.junit.Assert.*;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Order;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

public class TestAntXmlReport  extends JUnit4XmlTestBase {
  @Root(name = "failsafe-summary")
  @Order(elements = {"completed", "errors", "failures", "skipped", "failureMessage"})
  public static class MavenFailsafeSummaryModel_Local {
    @Attribute(required = false)
    public Integer result;

    @Attribute
    public boolean timeout = false;

    @Element
    public int completed;

    @Element
    public int errors;

    @Element
    public int failures;

    @Element
    public int skipped;

    @Element(required = false)
    public String failureMessage = "";
  }
  
  @Test 
  public void antxml() throws Exception {
    super.executeTarget("antxml");

    // Simple check for existence.
    Assert.assertTrue(
        new File(getProject().getBaseDir(), "ant-xmls/TEST-com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError.xml").length() > 0);

    Assert.assertTrue(
        new File(getProject().getBaseDir(), "ant-xmls/TEST-com.carrotsearch.ant.tasks.junit4.tests.replication.TestSuiteReplicated-2.xml").length() > 0);

    // Check for warning messages about duplicate suites.
    assertLogContains("Duplicate suite name used with XML reports");
    
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
  
  @Test 
  public void summary() throws Exception {
    super.executeTarget("antxml-summary");

    Persister p = new Persister();
    File parent = getProject().getBaseDir();

    MavenFailsafeSummaryModel_Local m1 = p.read(MavenFailsafeSummaryModel_Local.class, new File(parent, "ant-xmls2/summary1.xml"));
    assertEquals(255, (int) m1.result);
    assertEquals(5, m1.completed);
    assertEquals(2, m1.skipped);
    assertEquals(1, m1.errors);
    assertEquals(1, m1.failures);
    
    m1 = p.read(MavenFailsafeSummaryModel_Local.class, new File(parent, "ant-xmls2/summary2.xml"));
    assertEquals(null, m1.result);
    assertEquals(1, m1.completed);
    assertEquals(0, m1.skipped);
    assertEquals(0, m1.errors);
    assertEquals(0, m1.failures);

    m1 = p.read(MavenFailsafeSummaryModel_Local.class, new File(parent, "ant-xmls2/summary3.xml"));
    assertEquals(254, (int) m1.result);
    assertEquals(0, m1.completed);
    assertEquals(0, m1.skipped);
    assertEquals(0, m1.errors);
    assertEquals(0, m1.failures);
  }  
}
