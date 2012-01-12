package com.carrotsearch.ant.tasks.junit4.slave;

import java.io.*;
import java.nio.charset.Charset;

import com.google.common.collect.AbstractIterator;

/**
 * Iterates over lines from standard input. 
 */
class StdInLineIterator extends AbstractIterator<String> {
  private BufferedReader reader;

  public StdInLineIterator() {
    this.reader = new BufferedReader(
      new InputStreamReader(
          System.in,
          Charset.defaultCharset()));
  }

  @Override
  protected String computeNext() {
    try {
      String line = reader.readLine();
      return line != null ? line : endOfData();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
