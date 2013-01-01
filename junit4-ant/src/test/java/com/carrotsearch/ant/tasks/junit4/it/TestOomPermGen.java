package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Test;

public class TestOomPermGen  extends JUnit4XmlTestBase {
  @Test
  public void oom() {
    super.executeForkedTarget("oompermgen", 30 * 1000L);
    if (!getLog().contains("1 ignored (1 assumption)")) {
      assertLogContains("java.lang.OutOfMemoryError");
    }
  }
}
