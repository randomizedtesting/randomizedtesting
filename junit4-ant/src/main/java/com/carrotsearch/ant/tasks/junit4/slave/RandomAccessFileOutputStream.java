package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.*;

/**
 * An OutputStream that uses {@link RandomAccessFile} for writing.
 */
class RandomAccessFileOutputStream extends OutputStream {
  
  private RandomAccessFile raf;

  public RandomAccessFileOutputStream(File file) throws IOException {
    this.raf = new RandomAccessFile(file, "rw");
  }

  @Override
  public void write(int b) throws IOException {
    raf.write(b);
  }
  
  @Override
  public void write(byte[] b) throws IOException {
    raf.write(b);
  }
  
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    raf.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    // This was causing intermittent channel invalidations on Windows for
    // no apparent reason. Also, it shouldn't be a problem if we don't sync
    // with the disk (and use OS cache only)?
    // raf.getChannel().force(true);
  }

  @Override
  public void close() throws IOException {
    raf.close();
  }
}
