package com.carrotsearch.ant.tasks.junit4;

import java.io.File;
import java.net.URL;

import org.junit.*;

/**
 * Check JVM logging settings. Thet seem to use process descriptors not
 * {@link System} streams.
 */
@Ignore("https://github.com/carrotsearch/randomizedtesting/issues/87")
public class TestJvmLogging extends AntBuildFileTestBase {
  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }
  
  @Test
  public void jvmverbose() {
    executeTarget("jvmverbose");
  }
}
