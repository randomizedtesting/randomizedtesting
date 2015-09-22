package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;

import com.carrotsearch.ant.tasks.junit4.tests.TestAfterClassError;
import com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError;


public class TestMiscJUnit4Attributes extends JUnit4XmlTestBase {
  @Test 
  public void customprefix() {
    executeForkedTarget("customprefix");
  }

  @Test
  public void statuses() throws Throwable {
    expectBuildExceptionContaining("statuses", 
        "1 suite, 5 tests, 1 error, 1 failure, 2 ignored (1 assumption)");
  }

  @Test
  public void ignoredSuite() throws Throwable {
    executeTarget("ignoredSuite");
    assertLogContains("Tests summary: 1 suite (1 ignored), 0 tests");
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
  public void seedpassing() {
    executeTarget("seedpassing");
  }

  @Test
  public void seedpassingInvalid() {
    expectBuildExceptionContaining("seedpassing.invalid", "Not a valid seed chain");
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
    assertLogContains("Assignment hint: J0  (cost  2019) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestTwoSeconds (by ExecutionTimeBalancer)");
    assertLogContains("Assignment hint: J1  (cost  1002) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestOneSecond (by ExecutionTimeBalancer)");
    assertLogContains("Assignment hint: J1  (cost   501) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestHalfSecond (by ExecutionTimeBalancer)");
    assertLogContains("Assignment hint: J1  (cost     2) com.carrotsearch.ant.tasks.junit4.tests.sub2.TestZeroSeconds (by ExecutionTimeBalancer)");
  }

  @Test
  public void balancing_nohints() {
    executeTarget("balancing_nohints");
  }

  @Test
  public void mergehints() {
    executeTarget("mergehints");
  }
}
