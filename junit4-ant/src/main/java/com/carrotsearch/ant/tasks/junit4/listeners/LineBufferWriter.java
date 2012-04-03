package com.carrotsearch.ant.tasks.junit4.listeners;

import java.io.IOException;
import java.io.Writer;

/**
 * Flushes on line end.
 */
public class LineBufferWriter extends Writer {
  private static final char   LF = '\n';
  private static final char[] EOL = {LF};

  private final Writer sink;
  private final StringBuilder buffer = new StringBuilder();

  public LineBufferWriter(Writer sink) {
    this.sink = sink;
  }
  
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    for (int i = 0; i < len; i++) {
      char chr = cbuf[i + off];
      buffer.append(chr);
      if (chr == LF) {
        sink.write(buffer.toString());
        buffer.setLength(0);
      }
    }
  }
  
  @Override
  public void flush() throws IOException {
    if (buffer.length() > 0) {
      write(EOL);
    }
  }

  @Override
  public void close() throws IOException {
    sink.close();
  }
}

