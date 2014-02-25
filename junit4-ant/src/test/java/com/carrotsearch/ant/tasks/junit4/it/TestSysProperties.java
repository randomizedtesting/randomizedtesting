package com.carrotsearch.ant.tasks.junit4.it;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class TestSysProperties extends JUnit4XmlTestBase {
  @Test 
  public void failureProperty() {
    executeTarget("failureProperty");
  }

  @Test
  public void failureTypePassing() {
    executeTarget("failureTypePassing");
    assertLogContains("Throwable #1: com.carrotsearch.ant.tasks.junit4.tests.SyntheticException");
    assertLogContains("Tests summary: 1 suite, 1 test, 1 error");
    
    super.restoreSyserr.println(getLog());
  }

  @Test
  public void escaping() {
    executeTarget("escaping");
    assertLogContains("\"-Dsysprop.key2=abc def\"");
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
  public void iters() {
    executeTarget("iters");
    Pattern p = Pattern.compile("TestSuccess\\.alwaysPasses");
    Matcher matcher = p.matcher(getLog());
    int count = 0;
    while (matcher.find()) {
      count++;
    }
    Assert.assertEquals(5, count);
  }
}
