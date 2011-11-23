package com.carrotsearch.ant.tasks.junit4.events;

import java.io.*;

/**
 * Event serializer.
 */
public class Serializer {
  private final ObjectOutputStream os;

  public Serializer(OutputStream os) throws IOException {
    this.os = new ObjectOutputStream(os);
  }

  public void serialize(IEvent event) throws IOException {
    os.writeObject(event);
  }

  public ObjectOutputStream getOutputStream() {
    return os;
  }
}
