package com.carrotsearch.ant.tasks.junit4.events;


public class AppendStdOutEvent extends AbstractEvent implements IStreamEvent {
  private byte[] chunk;

  protected AppendStdOutEvent() {
    super(EventType.APPEND_STDOUT);
  }

  public AppendStdOutEvent(byte[] b, int off, int len) {
    this();
    chunk = new byte [len];
    System.arraycopy(b, off, chunk, 0, len);
  }

  public byte[] getChunk() {
    return chunk;
  }
}
