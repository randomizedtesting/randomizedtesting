package com.carrotsearch.ant.tasks.junit4;

import com.carrotsearch.ant.tasks.junit4.events.FailureEvent;
import com.carrotsearch.ant.tasks.junit4.events.IEvent;
import com.carrotsearch.ant.tasks.junit4.events.mirrors.FailureMirror;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Create a summary of tests execution.
 * 
 * @see EventBus
 */
public class TestsSummaryEventListener {
  private int failures;
  private int tests;
  private int errors;
  private int assumptions;
  private int ignores;

  /**
   * Subscribe to all events.
   */
  @Subscribe
  public void receiveEvent(IEvent e) {
    switch (e.getType()) {
      case TEST_STARTED: 
        tests++;
        break;

      case TEST_FAILURE:
      case SUITE_FAILURE:
        FailureMirror failure = ((FailureEvent) e).getFailure(); 
        if (failure.isAssertionViolation()) {
          failures++;
        } else {
          errors++;
        }
        break;

      case TEST_IGNORED_ASSUMPTION:
        assumptions++;
        ignores++; // tempting to fallthrough, isn't it? :)
        break;

      case TEST_IGNORED:
        ignores++;
        break;
    }
  }

  /**
   * Return the summary of all tests.
   */
  public TestsSummary getResult() {
    return new TestsSummary(tests, failures, errors, assumptions, ignores);
  }
}
