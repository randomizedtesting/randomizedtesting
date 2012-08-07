package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;

/**
 * Test things related to output capturing.
 */
public class TestOutputCapture extends JUnit4XmlTestBase {
  @Test
  public void sysstreams() {
    executeTarget("sysstreams");
    assertLogContains("Tests summary: 1 suite, 2 tests");
    assertLogContains("1> sysout-sysout-contd.");
    assertLogContains("2> syserr-syserr-contd.");
  }

  @Test
  public void outofordersysouts() {
    executeTarget("outofordersysouts");
  }

  @Test
  public void staticScopeOutput() {
    executeTarget("staticScopeOutput");
    assertLogContains("1> static-scope");
    assertLogContains("1> before-class");
    assertLogContains("1> after-class");
  }
}
