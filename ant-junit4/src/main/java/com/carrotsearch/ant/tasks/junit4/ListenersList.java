package com.carrotsearch.ant.tasks.junit4;

import java.util.List;

import com.carrotsearch.ant.tasks.junit4.listeners.AggregatedEventListener;

public class ListenersList {
  
  private List<Object> listeners;

  public ListenersList(List<Object> listeners) {
    this.listeners = listeners;
  }

  /**
   * Adds a listener to the listeners list.
   * @param listener
   */
  public void addConfigured(AggregatedEventListener listener) {
    listeners.add(listener);
  }
}
