package com.carrotsearch.randomizedtesting.examples;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.aspects.DiskProblems;
import com.carrotsearch.randomizedtesting.aspects.TrackTempDirLocks;

/**
 * This is an example of a "business class" that we will test using
 * {@link RandomizedRunner} and aspect to simulate real-life problems (
 * {@link DiskProblems}) or code issues ({@link TrackTempDirLocks}).
 */
public class IOUtils {
  /**
   * Read a file from disk and convert it to a Java String using the given
   * charset.
   */
  public static String readFile(File file, Charset charset) throws IOException {
    byte[] bytes = new byte[(int) file.length()];
    DataInputStream is = new DataInputStream(new FileInputStream(file));
    try {
      is.readFully(bytes);
    } finally {
      is.close(); // don't suppress IOException if it happens.
    }
    return new String(bytes, charset);
  }
}
