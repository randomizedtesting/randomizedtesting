package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Test;

public class TestOomCode  extends JUnit4XmlTestBase {
  @Test
  public void oom() {
    super.executeForkedTarget("oomcode");
    assertLogContains("Forked JVM ran out of memory");
    assertLogContains("WARN: JVM out of memory");
  }
}
