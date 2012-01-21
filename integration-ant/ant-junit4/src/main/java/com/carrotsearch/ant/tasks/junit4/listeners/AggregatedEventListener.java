package com.carrotsearch.ant.tasks.junit4.listeners;

import org.apache.tools.ant.BuildException;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.*;
import com.google.common.eventbus.EventBus;

/**
 * A dummy interface to indicate listener types for ANT. {@link JUnit4} uses
 * guava's {@link EventBus} to propagate events to listeners.
 * 
 * @see AggregatedSuiteResultEvent
 * @see AggregatedTestResultEvent
 * @see AggregatedQuitEvent
 */
public interface AggregatedEventListener {
  /**
   * Link to the container. Listener can throw {@link BuildException} if
   * parameter validation doesn't succeed, for example.
   */
  void setOuter(JUnit4 junit) throws BuildException;
}
