package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.*;

/**
 * An OutputStream delegate.
 */
class EventsOutputStream extends OutputStream {
  private OutputStream os;

  public EventsOutputStream(File file) throws IOException {
    final int bufferSize = 8 * 1024;
    this.os = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
  }

  @Override
  public void write(int b) throws IOException {
    os.write(b);
  }
  
  @Override
  public void write(byte[] b) throws IOException {
    os.write(b);
  }
  
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    os.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    os.flush();
  }

  @Override
  public void close() throws IOException {
    os.close();
  }
}
