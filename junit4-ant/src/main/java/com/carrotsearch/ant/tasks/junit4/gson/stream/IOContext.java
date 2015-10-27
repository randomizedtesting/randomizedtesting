package com.carrotsearch.ant.tasks.junit4.gson.stream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IOContext {
  private Map<Object, Object> context = new HashMap<Object, Object>();

  public void registerInContext(Object key, Object value) throws IOException {
    if (inContext(key)) {
      throw new IOException("Key already registered: " + key);
    }
    if (value == null) {
      throw new IOException("Value cannot be null for key: " + key);
    }
    context.put(key, value);
  }

  public Object lookupInContext(Object key) {
    return context.get(key);
  }
  
  public boolean inContext(Object key) {
    return context.containsKey(key);
  }
}
