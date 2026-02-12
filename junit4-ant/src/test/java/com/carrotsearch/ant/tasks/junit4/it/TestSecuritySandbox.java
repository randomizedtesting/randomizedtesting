package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.AssumptionViolatedException;
import org.junit.Test;

/** Test report-text listener. */
public class TestSecuritySandbox extends JUnit4XmlTestBase {
  @Test
  public void gh255() {
    ignoreAfterJava9();

    super.executeTarget("gh255");
    assertLogContains("access denied (\"java.util.PropertyPermission\" \"foo\" \"write\")");
    assertLogContains("Tests summary: 1 suite, 1 test, 1 error");
  }

  private void ignoreAfterJava9() {
    try {
      Class.class.getMethod("getModule");
      throw new AssumptionViolatedException("Won't work on Java9+");
    } catch (NoSuchMethodException e) {
      // Ok, pre-java 9.
    }
  }
}
