package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Ignore;
import org.junit.Test;

public class TestOomCode  extends JUnit4XmlTestBase {
  @Test
  @Ignore("Not portable across JVMs")
  public void oom() {
    super.executeForkedTarget("oomcode", 5 * 60 * 1000L);
    assertLogContains("Forked JVM ran out of memory");
    assertLogContains("WARN: JVM out of memory");
  }
}
