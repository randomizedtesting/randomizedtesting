package com.carrotsearch.ant.tasks.junit4;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * An event published when a slave is idle and waits for new suite classes.
 */
class SlaveIdle {
  private OutputStreamWriter stdin;

  /** For delegation. */
  SlaveIdle() {
  }
  
  public SlaveIdle(OutputStreamWriter stdin) {
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
