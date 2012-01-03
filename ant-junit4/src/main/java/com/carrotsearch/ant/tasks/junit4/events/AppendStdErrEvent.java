package com.carrotsearch.ant.tasks.junit4.events;

public class AppendStdErrEvent extends AbstractEvent {
  private byte[] chunk;

  public AppendStdErrEvent() {
    super(EventType.APPEND_STDERR);
  }
  
  public AppendStdErrEvent(byte[] b, int off, int len) {
    this();
    chunk = new byte [len];
    System.arraycopy(b, off, chunk, 0, len);
  }

  public byte[] getChunk() {
    return chunk;
  }
}