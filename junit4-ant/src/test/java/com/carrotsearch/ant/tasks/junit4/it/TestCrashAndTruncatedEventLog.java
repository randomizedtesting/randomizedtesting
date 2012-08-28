package com.carrotsearch.ant.tasks.junit4.it;


import org.junit.Test;

/**
 * Test heartbeat on slow, non-updating tests.
 */
public class TestCrashAndTruncatedEventLog extends JUnit4XmlTestBase {
  @Test
  public void testTruncatedLog() {
    executeTarget("truncatedlog");
  }
}
