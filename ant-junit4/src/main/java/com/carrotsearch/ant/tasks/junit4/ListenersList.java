package com.carrotsearch.ant.tasks.junit4;

import java.util.List;

import com.carrotsearch.ant.tasks.junit4.listeners.ConsoleInfoListener;

public class ListenersList {
  
  private List<Object> listeners;

  public ListenersList(List<Object> listeners) {
    this.listeners = listeners;
  }

  /**
   * Creates a named "consoleInfo" listener.
   */
  public ConsoleInfoListener createConsoleInfo() {
    return addListener(new ConsoleInfoListener());
  }

  /**
   * Adds a listener to the shared list. 
   */
  private <T> T addListener(T listener) {
    listeners.add(listener);
    return listener;
  }
}
