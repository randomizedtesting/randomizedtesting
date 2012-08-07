package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.OutputStream;

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

  @Override
  public void copyTo(OutputStream os) throws IOException {
    os.write(chunk);
  }
}
