package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Test;

public class TestCodeOOM  extends JUnit4XmlTestBase {
  @Test
  public void sysoutoom() {
    super.executeForkedTarget("codeoom");
    assertLogContains("Forked JVM ran out of memory");
    assertLogContains("WARN: JVM out of memory");
  }
}
