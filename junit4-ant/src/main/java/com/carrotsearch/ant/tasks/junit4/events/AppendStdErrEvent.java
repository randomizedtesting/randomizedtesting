package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.OutputStream;

public class AppendStdErrEvent extends AbstractEvent implements IStreamEvent {
  private byte[] chunk;

  protected AppendStdErrEvent() {
    super(EventType.APPEND_STDERR);
  }

  public AppendStdErrEvent(byte[] b, int off, int len) {
    this();
    chunk = new byte [len];
    System.arraycopy(b, off, chunk, 0, len);
  }

  @Override
  public void copyTo(OutputStream os) throws IOException {
    os.write(chunk);
  }
}