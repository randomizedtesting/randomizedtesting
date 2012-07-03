package com.carrotsearch.ant.tasks.junit4;


import org.junit.Test;

/**
 * Test heartbeat on slow, non-updating tests.
 */
public class TestHeartbeat extends JUnit4XmlTestBase {
  @Test
  public void testHeartbeat() {
    executeTarget("testHeartbeat");
    assertLogContains("HEARTBEAT J0");
    assertLogContains("approx. at: HeartbeatSlow.method1");
  }
}
