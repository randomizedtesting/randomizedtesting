package com.carrotsearch.ant.tasks.junit4.listeners;

import java.io.IOException;
import java.io.Writer;

import org.apache.tools.ant.util.LineOrientedOutputStream;

/**
 * Implements {@link LineOrientedOutputStream} which prefixes every new line
 * with a given byte [], synchronizing multiple streams to emit consistent lines.
 */
class PrefixedWriter extends Writer {
  private final static char LF = '\n';
  private final Writer sink;
  private final String prefix;
  private boolean atStart;

  public PrefixedWriter(String prefix, Writer sink) {
    super(sink);
    this.sink = sink;
    this.prefix = prefix;
    this.atStart = true;
  }

  @Override
  public void write(int c) throws IOException {
    if (atStart) {
      sink.write(prefix);
      atStart = false;
    }
    
    sink.write(c);
    if (c == LF) {
      sink.write(prefix);
    }
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    for (int i = off; i < off + len; i++) {
      write(cbuf[i]);
    }
  }

  @Override
  public void flush() throws IOException {
    sink.flush();
  }

  @Override
  public void close() throws IOException {
    sink.close();
  }
}
