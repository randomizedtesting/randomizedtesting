package com.carrotsearch.ant.tasks.junit4.events;

import java.io.*;

/**
 * Event deserializer.
 */
public class Deserializer {
  private final ObjectInputStream is;

  public Deserializer(InputStream is) throws IOException {
    this.is = new ObjectInputStream(is);
  }

  public IEvent deserialize() throws IOException {
    try {
      return (IEvent) is.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Event stream panic (class not found): "
          + e.toString());
    }
  }

  public ObjectInputStream getInputStream() {
    return is;
  }
}
