package com.carrotsearch.ant.tasks.junit4.listeners;

import com.carrotsearch.ant.tasks.junit4.JUnit4;
import com.carrotsearch.ant.tasks.junit4.events.QuitEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedSuiteResultEvent;
import com.carrotsearch.ant.tasks.junit4.events.aggregated.AggregatedTestResultEvent;
import com.google.common.eventbus.EventBus;

/**
 * A dummy interface to indicate listener types for ANT. {@link JUnit4} uses
 * guava's {@link EventBus} to propagate events to listeners.
 * 
 * @see AggregatedSuiteResultEvent
 * @see AggregatedTestResultEvent
 * @see QuitEvent
 */
public interface AggregatedEventListener {
  void setOuter(JUnit4 junit);
}
