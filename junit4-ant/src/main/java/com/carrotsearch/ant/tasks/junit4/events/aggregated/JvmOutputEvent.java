package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.io.File;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;

/**
 * An event emitted when there was any unexpected JVM output
 * from the forked JVM. This can happen for internal JVM logs
 * or crash dumps that bypass System.* streams.
 */
public class JvmOutputEvent {
  public final ForkedJvmInfo childInfo;
  public final File jvmOutput;

  public JvmOutputEvent(ForkedJvmInfo childInfo, File jvmOutput) {
    this.childInfo = childInfo;
    this.jvmOutput = jvmOutput;
  }
  
  public ForkedJvmInfo getSlave() {
    return childInfo;
  }

  public File getJvmOutputFile() {
    return jvmOutput;
  }
}
