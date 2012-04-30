package com.carrotsearch.ant.tasks.junit4;

import java.io.File;
import java.net.URL;

import org.junit.*;

/**
 * Check JVM logging settings. Thet seem to use process descriptors not
 * {@link System} streams.
 */
public class TestJvmLogging extends AntBuildFileTestBase {
  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }
  
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
