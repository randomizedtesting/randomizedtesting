package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Assert;
import org.junit.Test;


public class TestShutdownHookDeadlock extends JUnit4XmlTestBase {
  @Test
  public void slavehanging() {
    long start = System.currentTimeMillis();
    executeForkedTarget("shutdownhook", 120 * 1000L);
    long end = System.currentTimeMillis();

    // This isn't a strong assertion but it'll do here. If the execution time > 60 seconds
    // something is stinky.
    Assert.assertTrue(end - start < 60 * 1000);
  }
}
