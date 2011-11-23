package com.carrotsearch.ant.tasks.junit4;

import java.net.URL;

import org.apache.tools.ant.*;
import org.junit.Test;

public class TestJUnit4 extends BuildFileTest {
  private StringBuilder builder;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    assertNotNull(resource);
    configureProject(resource.getFile());

    builder = new StringBuilder();
    getProject().addBuildListener(new DefaultLogger() {
      @Override
      public void messageLogged(BuildEvent e) {
        builder.append(e.getPriority() + " ");
        builder.append(e.getMessage());
        builder.append("\n");
      }
    });
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    // System.out.println(builder.toString());
    builder = null;
  }

  @Test
  public void testNormalExecution() {
    super.executeTarget("normal");
  }

  @Test
  public void testDir() {
    super.executeTarget("dir");
  }

  @Test
  public void testMaxMem() {
    super.executeTarget("maxmem");
  }  

  @Test
  public void testJvmArg() {
    super.executeTarget("jvmarg");
  }
  
  @Test
  public void testSysProperty() {
    super.executeTarget("sysproperty");
  }

  @Test
  public void testEnv() {
    super.executeTarget("env");
  }

  @Test
  public void testFailing() {
    super.expectBuildExceptionContaining("failing", "tests failures", "1 error, 1 failure");
  }
  
  @Test
  public void testJvmCrash() {
    super.expectBuildExceptionContaining("jvmcrash", "crash log", "alternate error stream");
  }
  
  @Test
  public void testFailureProperty() {
    super.executeTarget("failureProperty");
  }
}
