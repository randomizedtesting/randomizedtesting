package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * An event published when a forked JVM is idle and waits for new suite classes.
 */
class ForkedJvmIdle {
  private OutputStreamWriter stdin;

  /** For delegation. */
  ForkedJvmIdle() {
  }
  
  public ForkedJvmIdle(OutputStreamWriter stdin) {
    this.stdin = stdin;
  }

  public void finished() {
    try {
      stdin.close();
    } catch (IOException e) {
      // Ignore, not much we can do.
    }
  }

  public void newSuite(String suiteName) {
    try {
      stdin.write(suiteName);
      stdin.write("\n");
      stdin.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
