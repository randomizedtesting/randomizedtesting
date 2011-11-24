package com.carrotsearch.ant.tasks.junit4.events.mirrors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("serial")
class SerializableMirror<T extends Serializable> implements Serializable {
  private byte[] bytes;

  public SerializableMirror(T t) {
    bytes = tryToSerialize(t);
  }

  protected byte[] getBytes() {
    return bytes;
  }
  
  /**
   * Attempt to reinstantiate the exception from serialized bytes.
   */
  @SuppressWarnings("unchecked")
  protected T getDeserialized() throws ClassNotFoundException, IOException {
    if (bytes == null)
      return null;

    ObjectInputStream is = new ObjectInputStream(
        new ByteArrayInputStream(bytes));
    return (T) is.readObject();
  }

  private static <E extends Serializable> byte[] tryToSerialize(E t) {
    if (t != null) {
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(t);
        oos.close();
        return os.toByteArray();
      } catch (Throwable ignore) {
        // Ignore.
      }
    }
    return null;
  }  
}
