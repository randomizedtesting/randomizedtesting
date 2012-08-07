package com.carrotsearch.ant.tasks.junit4.events;

import java.nio.ByteBuffer;

public class AppendStdOutEvent extends AbstractEvent implements IStreamEvent {
  public byte[] chunk;

  protected AppendStdOutEvent() {
    super(EventType.APPEND_STDOUT);
  }

  public AppendStdOutEvent(byte[] b, int off, int len) {
    this();
    chunk = new byte [len];
    System.arraycopy(b, off, chunk, 0, len);
  }

  public ByteBuffer getChunk() {
    return ByteBuffer.wrap(chunk);
  }
}
