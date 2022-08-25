package com.carrotsearch.ant.tasks.junit4.it;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


public class TestShutdownHookDeadlock extends JUnit4XmlTestBase {
  @Test
  public void forkedjvmhanging() {
    long start = System.nanoTime();
    executeForkedTarget("shutdownhook", 120 * 1000L);
    long end = System.nanoTime();

    // This isn't a strong assertion but it'll do here. If the execution time > 60 seconds
    // something is stinky.
    Assert.assertTrue(TimeUnit.NANOSECONDS.toMillis(end - start) < 60 * 1000);
  }
}
