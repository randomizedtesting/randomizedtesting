package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Test;

/**
 * Check JVM logging settings. Thet seem to use process descriptors not
 * {@link System} streams.
 */
public class TestJvmLogging extends JUnit4XmlTestBase {
  @Test
  public void jvmverbose() {
    executeTarget("jvmverbose");
    assertLogContains("was not empty, see");
  }
  
  @Test
  public void sysouts() {
    executeTarget("sysouts");
    assertLogContains("syserr-syserr-contd.");
    assertLogContains("sysout-sysout-contd.");
  }
}
