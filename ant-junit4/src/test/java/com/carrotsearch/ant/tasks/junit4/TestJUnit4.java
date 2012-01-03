package com.carrotsearch.ant.tasks.junit4;


import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.ant.tasks.junit4.tests.TestAfterClassError;
import com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError;


public class TestJUnit4 extends AntBuildFileTestBase {

  @Before
  public void setUp() throws Exception {
    URL resource = getClass().getClassLoader().getResource("junit4.xml");
    super.setupProject(new File(resource.getFile()));
  }

  @Test
  public void sysstreams() {
    executeTarget("sysstreams");
    assertLogContains("Tests summary: 1 suite, 2 tests");
    assertLogContains("1> sysout-sysout-contd.");
    assertLogContains("2> syserr-syserr-contd.");
  }

  @Test
  public void escaping() {
    executeTarget("escaping");
  }

  @Test 
  public void nojunit() {
    expectBuildExceptionContaining("nojunit", "Forked JVM's classpath must include a junit4 JAR");
  }

  @Test 
  public void oldjunit() {
    executeForkedTarget("oldjunit");
    assertLogContains("Forked JVM's classpath must use JUnit 4.10 or newer");
  }

  @Test 
  public void nojunit_task() {
    executeForkedTarget("nojunit-task");
    assertLogContains("JUnit JAR must be added to junit4 taskdef's classpath");
  }

  @Test 
  public void oldjunit_task() {
    executeForkedTarget("oldjunit-task");
    assertLogContains("At least JUnit version 4.10 is required on junit4's taskdef classpath");
  }

  @Test
  public void statuses() throws Throwable {
    expectBuildExceptionContaining("statuses", 
        "1 suite, 5 tests, 1 error, 1 failure, 2 ignored (1 assumption)");
  }

  @Test
  public void ignoredSuite() throws Throwable {
    executeTarget("ignoredSuite");
    assertLogContains("Tests summary: 1 suite, 0 tests");
  }

  @Test
  public void beforeClassError() throws Throwable {
    expectBuildExceptionContaining("beforeClassError", 
        "1 suite, 0 tests, 1 suite-level error");
    assertLogContains("| " + TestBeforeClassError.class.getSimpleName() + " (suite)");
  }

  @Test
  public void afterClassError() throws Throwable {
    expectBuildExceptionContaining("afterClassError", 
        "1 suite, 1 test, 1 suite-level error");
    assertLogContains("| " + TestAfterClassError.class.getSimpleName() + " (suite)");
  }

  @Test
  public void hierarchicalSuiteDescription() throws Throwable {
    expectBuildExceptionContaining("hierarchicalSuiteDescription", 
        "1 suite, 2 tests, 3 suite-level errors, 1 error");
  }

  @Test 
  public void dir() {
    executeTarget("dir");
  }
  
  @Test 
  public void maxmem() {
    executeTarget("maxmem");
  }  

  @Test 
  public void jvmarg() {
    executeTarget("jvmarg");
  }
  
  @Test 
  public void sysproperty() {
    executeTarget("sysproperty");
  }

  @Test 
  public void env() {
    executeTarget("env");
  }

  @Test 
  public void failureProperty() {
    executeTarget("failureProperty");
  }

  @Test
  public void failureTypePassing() {
    executeTarget("failureTypePassing");
    assertLogContains("Throwable #1: com.carrotsearch.ant.tasks.junit4.tests.SyntheticException");
    assertLogContains("Tests summary: 1 suite, 1 test, 1 error");
  }

  @Test
  public void jvmcrash() {
    expectBuildExceptionContaining("jvmcrash", "Unexpected output from forked JVM.");
    File cwd = getProject().getBaseDir();
    for (File crashDump : cwd.listFiles()) {
      if (crashDump.isFile() && 
          (crashDump.getName().matches("^hs_err_pid.+\\.log") ||
           crashDump.getName().endsWith(".mdmp"))) {
        crashDump.delete();
      }
    }
  }

  @Test
  public void seedpassing() {
    executeTarget("seedpassing");
  }

  @Test
  public void seedpassingInvalid() {
    expectBuildExceptionContaining("seedpassing.invalid", "Expected hexadecimal seed");
  }
  
  @Test
  public void reproducestring() {
    executeTarget("reproducestring");
    assertLogContains("2> Reproduce: ");
  }
  
  @Test
  public void assertions() {
    expectBuildExceptionContaining("assertions", "There were test failures");
    assertLogContains("> Throwable #1: java.lang.AssertionError: foobar");
  }

  @Test
  public void balancing() {
    executeTarget("balancing");
    assertLogContains("TestTwoSeconds assigned to slave 0");
    assertLogContains("TestOneSecond assigned to slave 1");
    assertLogContains("TestHalfSecond assigned to slave 1");
    assertLogContains("TestZeroSeconds assigned to slave 1");
  }
}
