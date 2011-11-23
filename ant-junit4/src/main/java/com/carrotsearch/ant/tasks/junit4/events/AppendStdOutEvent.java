package com.carrotsearch.ant.tasks.junit4.events;


@SuppressWarnings("serial")
public class AppendStdOutEvent extends AbstractEvent {
  private final byte[] chunk;

  public AppendStdOutEvent(byte[] b, int off, int len) {
    super(EventType.APPEND_STDOUT);
    chunk = new byte [len];
    System.arraycopy(b, off, chunk, 0, len);
  }

  public byte[] getChunk() {
    return chunk;
  }
}
