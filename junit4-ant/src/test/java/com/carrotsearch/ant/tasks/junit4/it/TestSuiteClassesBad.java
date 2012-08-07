package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;


public class TestSuiteClassesBad extends JUnit4XmlTestBase {
  @Test
  public void notaclass() {
    expectBuildExceptionContaining("notaclass",
        "File does not start with a class magic");
  }

  @Test
  public void notinstantiable() {
    expectBuildExceptionContaining("notinstantiable",
        "There were test failures: 1 suite, 1 test, 1 error");
  }
}
