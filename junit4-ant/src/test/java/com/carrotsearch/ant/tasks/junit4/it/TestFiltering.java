package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;


public class TestFiltering extends JUnit4XmlTestBase {
  @Test
  public void classfilter() {
    executeTarget("classfilter");
    assertLogContains("Tests summary: 1 suite");
  }
  
  @Test
  public void methodfilter() {
    executeTarget("methodfilter");
    assertLogContains("Tests summary: 1 suite, 2 tests");
  }
  
  @Test
  public void filterexpression() {
    executeTarget("filterexpression");

    assertLogContains("Parsed test filtering expression: (@foo AND (NOT @bar))");
    assertLogContains(">foo<");
    assertLogDoesNotContain(">foobar<");
    assertLogDoesNotContain(">bar<");
    assertLogContains("Tests summary: 1 suite, 3 tests, 2 ignored (2 assumptions)");
  }
}
