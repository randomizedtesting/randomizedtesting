package com.carrotsearch.ant.tasks.junit4;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * Flushes streams after starting a test and after the test finishes.
 */
class StreamFlusher extends RunListener {
  @Override
  public void testStarted(Description description) throws Exception {
    System.out.flush();
    System.err.flush();
  }

  @Override
  public void testFinished(Description description) throws Exception {
    System.out.flush();
    System.err.flush();
  }
}
