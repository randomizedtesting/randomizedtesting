package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.SlaveID;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.SuiteCompletedEvent;
import com.carrotsearch.ant.tasks.junit4.events.SuiteFailureEvent;
import com.carrotsearch.ant.tasks.junit4.events.SuiteStartedEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestFailureEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestFinishedEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestIgnoredAssumptionEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestIgnoredEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestStartedEvent;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Aggregates atomic events to higher-level events that contain a
 * full summary of a given test's execution. Simplifies reporting
 * logic.
 */
public class AggregatingListener {
  private EventBus target;
  private SlaveID slave;

  private Description lastSuite;
  private List<FailureMirror> suiteFailures;

  private ArrayDeque<AggregatedTestResultEvent> tests;
  private ArrayList<IEvent> eventStream;
  private int testStartStreamMarker;

  /**
   * @param target Which event bus to repost aggregated events to?
   */
  public AggregatingListener(EventBus target, SlaveID slave) {
    this.target = target;
    this.slave = slave;
  }

  @Subscribe
  public void appendToEventStream(IEvent e) {
    if (eventStream != null) {
      switch (e.getType()) {
        case APPEND_STDOUT:
        case APPEND_STDERR:
        case TEST_STARTED:
        case TEST_FINISHED:
        case TEST_FAILURE:
        case TEST_IGNORED:
        case TEST_IGNORED_ASSUMPTION:
        case SUITE_FAILURE:
          eventStream.add(e);
      }
    }
  }

  @Subscribe
  public void receiveSuiteStart(SuiteStartedEvent e) {
    assert lastSuite == null;
    tests = new ArrayDeque<AggregatedTestResultEvent>();
    suiteFailures = Lists.newArrayList();
    eventStream = Lists.newArrayList();
  }

  @Subscribe
  public void receiveTestStart(TestStartedEvent e) {
    tests.push(new AggregatedTestResultEvent(slave, lastSuite, e.getDescription()));
    testStartStreamMarker = eventStream.size();
  }

  @Subscribe
  public void receiveTestIgnored(TestIgnoredEvent e) {
    assert e.getDescription().equals(tests.peek().getDescription());
    tests.peek().setIgnored();
  }

  @Subscribe
  public void receiveTestFailure(TestIgnoredAssumptionEvent e) {
    assert e.getDescription().equals(tests.peek().getDescription());
    tests.peek().addFailure(e.getFailure());
  }

  @Subscribe
  public void receiveTestFailure(TestFailureEvent e) {
    assert e.getDescription().equals(tests.peek().getDescription());
    tests.peek().addFailure(e.getFailure());
  }

  @Subscribe
  public void receiveTestEnd(TestFinishedEvent e) {
    assert e.getDescription().equals(tests.peek().getDescription());
    tests.peek().complete(eventStream.subList(testStartStreamMarker, eventStream.size()));
  }

  @Subscribe
  public void receiveSuiteEnd(SuiteCompletedEvent e) {
    target.post(new AggregatedSuiteResultEvent(
        slave, e.getDescription(), suiteFailures, Lists.newArrayList(tests)));
    this.suiteFailures = null;
    this.lastSuite = null;
    this.tests = null;
    this.eventStream = null;
  }

  @Subscribe
  public void receiveSuiteFailure(SuiteFailureEvent e) {
    suiteFailures.add(e.getFailure());
  }
}
