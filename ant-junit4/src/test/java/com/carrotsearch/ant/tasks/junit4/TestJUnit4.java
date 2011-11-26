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
    getProject().executeTarget("sysstreams");
    assertLogContains("Tests summary: 1 suite, 2 tests");
    assertLogContains("1> sysout-sysout-contd.");
    assertLogContains("2> syserr-syserr-contd.");
  }

  @Test 
  public void nojunit() {
    expectBuildExceptionContaining("nojunit", "Forked JVM's classpath must include a junit4 JAR");
  }

  @Test
  public void statuses() throws Throwable {
    expectBuildExceptionContaining("statuses", 
        "1 suite, 5 tests, 1 error, 1 failure, 2 ignored (1 assumption)");
  }

  @Test
  public void ignoredSuite() throws Throwable {
    getProject().executeTarget("ignoredSuite");
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

  
  
  
  
  /*
  @Test
  public void testFailing() throws Throwable {
    try {
      super.expectBuildExceptionContaining("failing", "tests failures", "1 suite-level error, 1 error, 1 failure");
    } finally {
      System.out.println(builder.toString());
      System.out.println(super.getOutput());
      System.out.println(super.getError());
    }
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
  public void testJvmCrash() {
    super.expectBuildExceptionContaining("jvmcrash", "crash log", "alternate error stream");
  }
  
  @Test 
  public void testFailureProperty() {
    super.executeTarget("failureProperty");
  }

  @Test 
  public void testNoJUnitOnClasspath() {
    super.expectBuildExceptionContaining("nojunit", "junit4 message", "must include a junit4");
  }
  */  
}
