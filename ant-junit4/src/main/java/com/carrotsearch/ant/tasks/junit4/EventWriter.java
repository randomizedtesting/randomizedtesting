package com.carrotsearch.ant.tasks.junit4;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * A handler for {@link IExecutionListener} methods 
 * that serializes all calls to an {@link OutputStream}.
 */
final class EventWriter implements InvocationHandler, Closeable {
  private final ObjectOutputStream os;

  public EventWriter(OutputStream os) {
    try {
      this.os = new ObjectOutputStream( 
          (os instanceof BufferedOutputStream ? os : new BufferedOutputStream(os)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    os.writeObject(method.getName());
    os.writeObject(args);
    return null;
  }

  @Override
  public void close() throws IOException {
    os.close();
  }
}
