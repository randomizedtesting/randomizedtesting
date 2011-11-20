package com.carrotsearch.ant.tasks.junit4;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A handler for {@link IExecutionListener} methods 
 * that serializes all calls to an {@link OutputStream}.
 */
final class EventReader implements Closeable {
  private final ObjectInputStream is;
  private final Object target;
  private final Map<String,Method> methods;

  public <T> EventReader(InputStream is, T target, Class<T> replayInterface) {
    try {
      this.is = new ObjectInputStream(is);
      this.target = target;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Map<String, Method> methods = new HashMap<String,Method>();
    for (Method m : replayInterface.getMethods()) {
      if (methods.containsKey(m.getName())) {
        throw new IllegalArgumentException("Overloaded methods not supported: "
            + m + ", " + methods.get(m.getName()));
      }
      methods.put(m.getName(), m);
    }
    this.methods = methods;
  }

  public void replay() throws IOException {
    try {
      do {
        String methodName = (String) is.readObject();
        Object[] args = (Object[]) is.readObject();
        
        Method m = methods.get(methodName);
        if (m == null) {
          throw new RuntimeException("Panic: no such method: " + methodName);
        }
        m.invoke(target, args);
      } while (true);
    } catch (EOFException e) {
      // Expected, EOF.
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    is.close();
  }
}
