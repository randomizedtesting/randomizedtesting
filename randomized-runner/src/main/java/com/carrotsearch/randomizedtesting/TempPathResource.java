package com.carrotsearch.randomizedtesting;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A temporary path resource will be deleted at the end of a given lifecycle phase.
 * 
 * @see RandomizedContext#closeAtEnd(Closeable, LifecycleScope)
 * @see RandomizedTest#newTempDir()
 */
public class TempPathResource implements Closeable {
  private final Path location;

  public TempPathResource(Path location) {
    this.location = location;
  }

  public void close() throws IOException {
    RandomizedTest.forceDeleteRecursively(location);
    if (Files.exists(location))
      throw new IOException("Could not remove path: "
          + location.toAbsolutePath());
  }
}
