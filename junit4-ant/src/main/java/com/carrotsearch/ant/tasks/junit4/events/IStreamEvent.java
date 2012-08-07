package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.OutputStream;

public interface IStreamEvent {
  public void copyTo(OutputStream os) throws IOException;
}
