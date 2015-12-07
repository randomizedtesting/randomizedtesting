package com.carrotsearch.ant.tasks.junit4.listeners;

import java.io.IOException;
import java.io.Writer;


class LineFlushingWriter extends Writer {
  private Writer w;

  public LineFlushingWriter(Writer w) {
    this.w = w;
  }

  @Override
  public void write(int c) throws IOException {
    w.write(c);
    if (c == '\n') flush();
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    w.write(cbuf, off, len);
    for (int i = 0; i < len; i++) {
      if (cbuf[off + i] == '\n') {
        flush();
        break;
      }
    }
  }

  @Override
  public void write(String str, int off, int len) throws IOException {
    w.write(str, off, len);
    for (int i = 0; i < len; i++) {
      if (str.charAt(off + i) == '\n') {
        flush();
        break;
      }
    }
  }
  
  @Override
  public Writer append(char c) throws IOException {
    w.append(c);
    if (c == '\n') flush();
    return this;
  }
  
  @Override
  public Writer append(CharSequence csq) throws IOException {
    w.append(csq);
    for (int i = 0, len = csq.length(); i < len; i++) {
      if (csq.charAt(i) == '\n') {
        flush();
        break;
      }
    }
    return this;
  }

  @Override
  public Writer append(CharSequence csq, int start, int end) throws IOException {
    w.append(csq, start, end);
    for (int i = start; i < end; i++) {
      if (csq.charAt(i) == '\n') {
        flush();
        break;
      }
    }
    return this;
  }

  @Override
  public void flush() throws IOException {
    w.flush();
  }

  @Override
  public void close() throws IOException {
    w.close();
  }
}
