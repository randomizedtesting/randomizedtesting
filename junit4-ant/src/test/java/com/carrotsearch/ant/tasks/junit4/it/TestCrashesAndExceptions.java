package com.carrotsearch.ant.tasks.junit4.it;


import java.io.File;

import org.apache.tools.ant.BuildException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;


public class TestCrashesAndExceptions extends JUnit4XmlTestBase {
  @Test
  public void forkedjvmhanging() {
    executeTarget("forkedjvmhanging");
    assertLogContains("Caused by: java.lang.ArithmeticException");
  }
  

  @Test
  public void jvmcrash() {
    try {
      executeTarget("jvmcrash");
      Assert.fail("Expected a build failure.");
    } catch (BuildException e) {
      String log = getLog();
      if (log.contains("java.lang.UnsatisfiedLinkError: Could not link with crashlib")) {
        // ignore
        Assume.assumeTrue(false);
      }
      Assert.assertThat(e.getMessage(), containsString("was not empty, see:"));
    }

    File cwd = getProject().getBaseDir();
    for (File crashDump : cwd.listFiles()) {
      if (crashDump.isFile() && 
          (crashDump.getName().matches("^hs_err_pid.+\\.log") ||
           crashDump.getName().endsWith(".mdmp") ||
           crashDump.getName().endsWith(".dmp") ||
           crashDump.getName().endsWith(".dump") ||
           crashDump.getName().endsWith(".trc"))) {
        crashDump.delete();
      }
    }
  }  
}
