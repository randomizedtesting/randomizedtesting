package com.carrotsearch.ant.tasks.junit4;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * An input stream that tails from a random access file as new input appears there.
 * It's a lousy solution but we don't have access to real interprocess pipes from Java.
 */
class TailInputStream extends InputStream {
  /** How long to sleep (millis) before checking for updates? */
  private static final long TAIL_CHECK_DELAY = 250;

  private final RandomAccessFile raf;
  private volatile boolean closed;
  private volatile boolean complete;

  public TailInputStream(Path file) throws FileNotFoundException {
    this.raf = new RandomAccessFile(file.toFile(), "r");
  }

  @Override
  public int read() throws IOException {
    if (closed) return -1;

    try {
      int c;
      while ((c = raf.read()) == -1) {
        if (complete) {
          return -1; // EOF;
        }
        try {
          Thread.sleep(TAIL_CHECK_DELAY);
        } catch (InterruptedException e) {
          throw new IOException(e);
        }
      }
      return c;
    } catch (IOException e) {
      if (closed) 
        return -1;
      else
        throw e;
    }
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (closed) return -1;

    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    try {
      int rafRead = raf.read(b, off, len);
      if (rafRead == -1) {
        // If nothing in the buffer, wait.
        do {
          if (complete) {
            return -1; // EOF;
          }

          try {
            Thread.sleep(TAIL_CHECK_DELAY);
          } catch (InterruptedException e) {
            throw new IOException(e);
          }
        } while ((rafRead = raf.read(b, off, len)) == -1);
      }
      return rafRead;
    } catch (IOException e) {
      if (closed) 
        return -1;
      else
        throw e;
    }
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
  
  @Override
  public boolean markSupported() {
    return false;
  }

  @Override
  public void close() throws IOException {
    closed = true;
    this.raf.close();
  }

  /**
   * Changes the semantics of tailing so that from the moment of calling this method on,
   * hitting an EOF on the tailed file will cause an EOF in read methods.
   */
  public void completeAtEnd() {
    complete = true;
  }
}
