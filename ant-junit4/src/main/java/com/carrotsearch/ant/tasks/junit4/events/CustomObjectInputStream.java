package com.carrotsearch.ant.tasks.junit4.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import com.google.common.collect.Maps;

class CustomObjectInputStream extends ObjectInputStream {
  /** table mapping primitive type names to corresponding class objects */
  private static final HashMap<String,Class<?>> primClasses = Maps.newHashMap();
  static {
    primClasses.put("boolean", boolean.class);
    primClasses.put("byte", byte.class);
    primClasses.put("char", char.class);
    primClasses.put("short", short.class);
    primClasses.put("int", int.class);
    primClasses.put("long", long.class);
    primClasses.put("float", float.class);
    primClasses.put("double", double.class);
    primClasses.put("void", void.class);
  }
  
  private final ClassLoader classLoader;

  public CustomObjectInputStream(InputStream is, ClassLoader ref) throws IOException {
    super(is);
    this.classLoader = ref;
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
      ClassNotFoundException {
    String name = desc.getName();
    try {
      return Class.forName(name, false, classLoader);
    } catch (ClassNotFoundException ex) {
      Class<?> cl = (Class<?>) primClasses.get(name);
      if (cl != null) {
        return cl;
      } else {
        throw ex;
      }
    }
  }
  
  @Override
  protected Class<?> resolveProxyClass(String[] interfaces) throws IOException,
      ClassNotFoundException {
    Class<?>[] interfaceClasses = new Class<?>[interfaces.length];
    for (int i = 0; i < interfaceClasses.length; i++) {
      interfaceClasses[i] = Class.forName(interfaces[i], false, classLoader);
    }
    return Proxy.getProxyClass(classLoader, interfaceClasses);
  }
}