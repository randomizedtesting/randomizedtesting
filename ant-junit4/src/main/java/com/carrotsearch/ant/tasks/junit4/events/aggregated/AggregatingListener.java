package com.carrotsearch.ant.tasks.junit4.events.aggregated;

import java.util.HashMap;
import java.util.List;

import org.junit.runner.Description;

import com.carrotsearch.ant.tasks.junit4.SlaveID;
import com.carrotsearch.ant.tasks.junit4.events.SuiteCompletedEvent;
import com.carrotsearch.ant.tasks.junit4.events.SuiteFailureEvent;
import com.carrotsearch.ant.tasks.junit4.events.SuiteStartedEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestFinishedEvent;
import com.carrotsearch.ant.tasks.junit4.events.TestStartedEvent;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

  /**
   * Last started suite.
   */
  private Description lastSuite;
  private List<TestExecutionResultEvent> tests;
  private List<FailureMirror> suiteFailures;

  /**
   * @param target Which event bus to repost aggregated events to?
   */
  public AggregatingListener(EventBus target, SlaveID slave) {
    this.target = target;
    this.slave = slave;
  }

  @Subscribe
  public void receiveSuiteStart(SuiteStartedEvent e) {
    assert lastSuite == null;
    tests = Lists.newArrayList();
    suiteFailures = Lists.newArrayList();
  }

  @Subscribe
  public void receiveTestStart(TestStartedEvent e) {
    
  }

  @Subscribe
  public void receiveTestIgnored(TestStartedEvent e) {
  }

  @Subscribe
  public void receiveTestFailure(TestStartedEvent e) {
  }

  @Subscribe
  public void receiveTestEnd(TestFinishedEvent e) {
  }

  @Subscribe
  public void receiveSuiteEnd(SuiteCompletedEvent e) {
    target.post(new SuiteExecutionResultEvent(suiteFailures, tests));
    this.suiteFailures = null;
    this.lastSuite = null;
    this.tests = null;
  }
  
  @Subscribe
  public void receiveSuiteFailure(SuiteFailureEvent e) {
    suiteFailures.add(e.getFailure());
  }
}
