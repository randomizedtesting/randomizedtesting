package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Ignore;
import org.junit.Test;

public class TestOomPermGen  extends JUnit4XmlTestBase {
  @Test
  @Ignore("Not portable across JVMs")
  public void oom() {
    super.executeForkedTarget("oompermgen", 5 * 60 * 1000L);
    if (!getLog().contains("1 ignored (1 assumption)")) {
      assertLogContains("java.lang.OutOfMemoryError");
    }
  }
}
