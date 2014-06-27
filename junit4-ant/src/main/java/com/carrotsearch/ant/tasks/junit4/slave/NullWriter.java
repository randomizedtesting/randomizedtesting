package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.IOException;
import java.io.Writer;


final class NullWriter extends Writer {
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }
}
