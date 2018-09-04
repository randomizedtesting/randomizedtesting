package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Ignore;
import org.junit.Test;


public class TestOnNonEmptyWorkDirectory extends JUnit4XmlTestBase {
  @Test 
  public void actionFail() {
    expectBuildExceptionContaining("onNonEmptyWorkDirectory-fail", 
        "existing-dir",
        "Cwd of a forked JVM already exists and is not empty");
  }
  
  @Test 
  public void actionWipe() {
    executeTarget("onNonEmptyWorkDirectory-wipe");
    assertLogContains("Cwd of a forked JVM already exists and is not empty, trying to wipe");
  }
  
  @Test 
  public void actionIgnore() {
    executeTarget("onNonEmptyWorkDirectory-ignore");
    assertLogContains("Cwd of a forked JVM already exists and is not empty");
  }

  // https://github.com/randomizedtesting/randomizedtesting/issues/247
  @Test
  public void tmpDirUnderCwd() {
    executeTarget("onNonEmptyWorkDirectory-tmpDirUnderCwd");
    assertLogContains("Created tmpfile");
  }
}
