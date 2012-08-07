package com.carrotsearch.ant.tasks.junit4.it;


import java.io.File;

import org.junit.Test;


public class TestCrashesAndExceptions extends JUnit4XmlTestBase {
  @Test
  public void slavehanging() {
    executeTarget("slavehanging");
    assertLogContains("Caused by: java.lang.ArithmeticException");
  }
  

  @Test
  public void jvmcrash() {
    expectBuildExceptionContaining("jvmcrash", "was not empty, see:");
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
