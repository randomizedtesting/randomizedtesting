package com.carrotsearch.ant.tasks.junit4;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link InvocationHandler} that multiplexes a given method call to all 
 * given objects. It is assumed that all methods of <code>T</code> have 
 * void return type.
 */
public class Multiplexer<T> implements InvocationHandler {
  private final List<? extends T> targets;

  private Multiplexer(Class<T> clazz, List<? extends T> targets) {
    checkAsserts(clazz);
    this.targets = targets;
  }

  @Override
  public synchronized final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    for (T target : targets) {
      try {
        method.invoke(target, args);
      } catch (Throwable t) {
        logError(t, method);
      }
    }
    return null;
  }

  /**
   * Log dispatch error.
   */
  protected void logError(Throwable t, Method m) {
    Logger.getAnonymousLogger().log(Level.SEVERE, 
        "An exception occurred dispatching method: " + m, t);
  }
  
  /**
   * Ensure contracts are valid. 
   */
  private static void checkAsserts(Class<?> clazz) {
    if (!clazz.isInterface())
      throw new IllegalArgumentException("Not an interface: " + clazz);
    
    for (Method m : clazz.getMethods()) {
      if (!m.getReturnType().equals(void.class)) {
        throw new IllegalArgumentException("Expected all methods to have a void return " +
        		"type but this method does not: " + m.toString());
      }
    }
  }

  /**
   * Create a multiplexer for a set of targets and a common interface.
   */
  public static <T> InvocationHandler forInterface(
      Class<T> clazz, List<? extends T> targets) {
    return new Multiplexer<T>(clazz, targets);
  }
}
