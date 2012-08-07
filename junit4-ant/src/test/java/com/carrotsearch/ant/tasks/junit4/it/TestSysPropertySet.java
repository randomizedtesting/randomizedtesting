package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Test;

public class TestSysPropertySet extends JUnit4XmlTestBase {
  @Test
  public void altVendors() {
    getProject().setProperty("prefix.dummy1", "value1");
    getProject().setProperty("prefix.dummy2", "value2");
    
    executeTarget("syspropertyset");
    
    assertLogContains("value1");
    assertLogContains("value2");
  }
}
