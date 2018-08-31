package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class SimpleStreamPumper implements Runnable {
  private final InputStream from;
  private final OutputStream to;

  public SimpleStreamPumper(InputStream from, OutputStream to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public void run() {
    try {
      byte[] buffer = new byte[1024 * 4];
      while (true) {
        int len = from.read(buffer);
        if (len > 0) {
          to.write(buffer, 0, len);
        } else if (len < 0) {
          break; // EOF.
        } else {
          // read should be blocking?
          Thread.sleep(250);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException("Pumper threads should not be interrupted?", e);
    }
  }
}
