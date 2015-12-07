package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;

import com.carrotsearch.ant.tasks.junit4.ForkedJvmInfo;
import com.carrotsearch.ant.tasks.junit4.events.*;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Aggregates atomic events from {@link JUnitCore} to higher-level events that
 * contain a full summary of a given test's execution. Simplifies reporting
 * logic.
 */
public class AggregatingListener {
  private EventBus target;
  private ForkedJvmInfo slave;

  private AggregatedSuiteStartedEvent startEvent;
  private Description lastSuite;
  private List<FailureMirror> suiteFailures;

  private ArrayDeque<AggregatedTestResultEvent> tests;
  private ArrayList<IEvent> eventStream;
  private int testStartStreamMarker;

  /**
   * @param target Which event bus to repost aggregated events to?
   */
  public AggregatingListener(EventBus target, ForkedJvmInfo slave) {
    this.target = target;
    this.slave = slave;
  }

  @Subscribe
  public void appendToEventStream(IEvent e) {
    if (eventStream != null) {
      switch (e.getType()) {
        case APPEND_STDOUT:
        case APPEND_STDERR:
          target.post(new PartialOutputEvent(slave, e));
          // fall through.
        case TEST_STARTED:
        case TEST_FINISHED:
        case TEST_FAILURE:
        case TEST_IGNORED:
        case TEST_IGNORED_ASSUMPTION:
        case SUITE_FAILURE:
          eventStream.add(e);
          break;
        default:
          break;
      }
    }
  }

  /**
   * Detect slow heartbeat (long time without any events) from the forked JVM.
   */
  @Subscribe
  public void slowHeartBeat(LowLevelHeartBeatEvent e) {
    Description current = null;
    if (tests != null && !tests.isEmpty()) {
      current = tests.peek().getDescription();
    } else {
      current = lastSuite; // may be null.
    }

    target.post(new HeartBeatEvent(
        slave,
        current,
        e.lastActivity,
        e.currentTime
    ));
  }

  @Subscribe
  public void receiveSuiteStart(SuiteStartedEvent e) {
    assert lastSuite == null;
    assert startEvent == null;

    tests = new ArrayDeque<AggregatedTestResultEvent>();
    suiteFailures = new ArrayList<>();
    eventStream = new ArrayList<>();
    lastSuite = e.getDescription();
    startEvent = new AggregatedSuiteStartedEvent(slave, e);

    target.post(startEvent);
  }

  @Subscribe
  public void receiveTestStart(TestStartedEvent e) {
    tests.push(new AggregatedTestResultEvent(slave, lastSuite, e.getDescription()));
    testStartStreamMarker = eventStream.size();
  }

  @Subscribe
  public void receiveTestIgnored(TestIgnoredEvent e) {
    Description description = e.getDescription();
    if (description.getMethodName() == null) {
      // This is how JUnit signals ignored suites: by passing a Description that is a 
      // suite but has no children (so isSuite() returns false...).
      return;
    }

    // Test ignored is not emitted within start...end space with default JUnit runners.
    // Try to correct it here.
    if (!tests.isEmpty() && description.equals(tests.peek().getDescription())) {
      tests.peek().setIgnored(e.getCause());
    } else {
      receiveTestStart(new TestStartedEvent(description));
      tests.peek().setIgnored(e.getCause());
      receiveTestEnd(new TestFinishedEvent(description, 0, e.getStartTimestamp()));
    }
  }

  @Subscribe
  public void receiveTestAssumptionIgnored(TestIgnoredAssumptionEvent e) {
    Description description = e.getDescription();
    if (description.getMethodName() == null) {
      // Don't record suite-level assumptions. They result in ignored
      // tests that RandomizedRunner reports and JUnit runner ignores
      // as if a suite didn't have any tests.
      return;
    }

    assert e.getDescription().equals(tests.peek().getDescription());
    tests.peek().addFailure(e.getFailure());
  }

  @Subscribe
  public void receiveTestFailure(TestFailureEvent e) {
    Description description = e.getDescription();
    if (description.getMethodName() == null) {
      suiteFailures.add(e.getFailure());
      return;
    }

    tests.peek().addFailure(e.getFailure());
  }

  @Subscribe
  public void receiveTestEnd(TestFinishedEvent e) {
    assert e.getDescription().equals(tests.peek().getDescription());
    tests.peek().complete(e, new ArrayList<>(eventStream.subList(testStartStreamMarker, eventStream.size())));
    target.post(tests.peek());
  }

  @Subscribe
  public void receiveSuiteEnd(SuiteCompletedEvent e) {
    target.post(new AggregatedSuiteResultEvent(
        startEvent,
        slave, 
        e.getDescription(), 
        suiteFailures, 
        Lists.newArrayList(tests.descendingIterator()), 
        eventStream,
        e.getStartTimestamp(), 
        e.getExecutionTime()));
    this.suiteFailures = null;
    this.lastSuite = null;
    this.startEvent = null;
    this.tests = null;
    this.eventStream = null;
  }

  @Subscribe
  public void receiveSuiteFailure(SuiteFailureEvent e) {
    if (suiteFailures != null) {
      suiteFailures.add(e.getFailure());
    } else {
      receiveSuiteStart(new SuiteStartedEvent(e.getDescription(), System.currentTimeMillis()));
      suiteFailures.add(e.getFailure());
      receiveSuiteEnd(new SuiteCompletedEvent(e.getDescription(), System.currentTimeMillis(), 0));
    }
  }
}
