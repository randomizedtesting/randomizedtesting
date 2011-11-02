package com.carrotsearch.randomizedtesting;

import java.io.*;

/**
 * A temporary path resource will be deleted at the end of a given lifecycle phase.
 * 
 * @see RandomizedContext#closeAtEnd(Closeable, LifecycleScope)
 * @see RandomizedTest#newTempDir()
 */
public class TempPathResource implements Closeable {
  private final File location;

  public TempPathResource(File location) {
    this.location = location;
  }

  public void close() throws IOException {
    RandomizedTest.forceDeleteRecursively(location);
    if (location.exists())
      throw new IOException("Could not remove path: "
          + location.getAbsolutePath());
  }
}
