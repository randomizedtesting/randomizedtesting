package com.carrotsearch.randomizedtesting.listeners;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements {@link LineOrientedOutputStream} which prefixes every new line
 * with a given byte [], synchronizing multiple streams to emit consistent lines.
 */
class PrefixedOutputStream extends LineOrientedOutputStream {
  private final OutputStream sink;
  private final AtomicBoolean mutex;
  private final byte[] prefix;
  
  public PrefixedOutputStream(byte[] prefix, AtomicBoolean mutex, OutputStream sink) {
    this.prefix = prefix;
    this.sink = sink;
    this.mutex = mutex;
  }
  
  @Override
  protected void processLine(byte[] line) throws IOException {
    synchronized (mutex) {
      if (!mutex.getAndSet(true)) {
        firstOutput(sink);
      }
      
      sink.write(prefix);
      sink.write(line);
      sink.write('\n');
    }
  }

  protected void firstOutput(OutputStream sink) throws IOException {
    // None;
  }
}
