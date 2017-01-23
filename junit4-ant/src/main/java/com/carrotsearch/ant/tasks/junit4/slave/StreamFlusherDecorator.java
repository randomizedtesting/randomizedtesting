package com.carrotsearch.ant.tasks.junit4.slave;

import org.junit.runner.notification.RunListener;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;

/**
 * Flushes {@link System#out} and {@link System#err} before
 * passing the event to the delegate.
 */
public final class StreamFlusherDecorator extends BeforeAfterRunListenerDecorator {
  public StreamFlusherDecorator(RunListener delegate) {
    super(delegate);
  }

  @SuppressForbidden("legitimate sysstreams.")
  @Override
  protected void before() {
    System.out.flush();
    System.err.flush();
  }
}
