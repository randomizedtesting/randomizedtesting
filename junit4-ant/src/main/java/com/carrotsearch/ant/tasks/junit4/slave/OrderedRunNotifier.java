package com.carrotsearch.ant.tasks.junit4.slave;

import java.util.*;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.*;

public class OrderedRunNotifier extends RunNotifier {
  /**
   * A linked list is more convenient for descending iterators. 
   */
  private final LinkedList<RunListener> listeners = new LinkedList<RunListener>();
  private volatile boolean stopRequested = false;

  /**
   * Add a listener at the end of the listener list.
   */
  public void addListener(RunListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }
  
  /**
   * Adds a listener at the head of the listener list.
   */
  public void addFirstListener(RunListener listener) {
    synchronized (listeners) {
      listeners.addFirst(listener);
    }
  }

  /**
   * Remove a listener from the listener list.
   */
  public void removeListener(RunListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }
  
  /**
   * Notify listeners in the requested order
   */
  private abstract class SafeNotifier {
    void run() {
      run(false);
    }

    void run(boolean reversedOrder) {
      synchronized (listeners) {
        final Iterator<RunListener> i;
        if (reversedOrder) {
          i = listeners.descendingIterator();
        } else {
          i = listeners.iterator();
        }

        while (i.hasNext()) {
          try {
            notifyListener(i.next());
          } catch (Exception e) {
            i.remove(); // Remove the offending listener.
            fireTestFailure(new Failure(Description.TEST_MECHANISM, e));
          }
        }
      }
    }

    abstract protected void notifyListener(RunListener each) throws Exception;
  }

  /**
   * Do not invoke.
   */
  public void fireTestRunStarted(final Description description) {
    new SafeNotifier() {
      @Override
      protected void notifyListener(RunListener each) throws Exception {
        each.testRunStarted(description);
      };
    }.run();
  }
  
  /**
   * Do not invoke.
   */
  public void fireTestRunFinished(final Result result) {
    new SafeNotifier() {
      @Override
      protected void notifyListener(RunListener each) throws Exception {
        each.testRunFinished(result);
      };
    }.run(true);
  }
  
  /**
   * Invoke to tell listeners that an atomic test is about to start.
   * 
   * @param description
   *          the description of the atomic test (generally a class and method
   *          name)
   * @throws StoppedByUserException
   *           thrown if a user has requested that the test run stop
   */
  public void fireTestStarted(final Description description) throws StoppedByUserException {
    if (stopRequested) throw new StoppedByUserException();

    new SafeNotifier() {
      @Override
      protected void notifyListener(RunListener each) throws Exception {
        each.testStarted(description);
      };
    }.run();
  }
  
  /**
   * Invoke to tell listeners that an atomic test failed.
   * 
   * @param failure
   *          the description of the test that failed and the exception thrown
   */
  public void fireTestFailure(final Failure failure) {
    new SafeNotifier() {
      @Override
      protected void notifyListener(RunListener each) throws Exception {
        each.testFailure(failure);
      };
    }.run(true);
  }
  
  /**
   * Invoke to tell listeners that an atomic test flagged that it assumed
   * something false.
   * 
   * @param failure
   *          the description of the test that failed and the
   *          {@link AssumptionViolatedException} thrown
   */
  public void fireTestAssumptionFailed(final Failure failure) {
    new SafeNotifier() {
      @Override
      protected void notifyListener(RunListener each) throws Exception {
        each.testAssumptionFailure(failure);
      };
    }.run(true);
  }
  
  /**
   * Invoke to tell listeners that an atomic test was ignored.
   * 
   * @param description
   *          the description of the ignored test
   */
  public void fireTestIgnored(final Description description) {
    new SafeNotifier() {
      @Override
      protected void notifyListener(RunListener each) throws Exception {
        each.testIgnored(description);
      }
    }.run(true);
  }
  
  /**
   * Invoke to tell listeners that an atomic test finished. Always invoke
   * {@link #fireTestFinished(Description)} if you invoke
   * {@link #fireTestStarted(Description)} as listeners are likely to expect
   * them to come in pairs.
   * 
   * @param description
   *          the description of the test that finished
   */
  public void fireTestFinished(final Description description) {
    new SafeNotifier() {
      @Override
      protected void notifyListener(RunListener each) throws Exception {
        each.testFinished(description);
      };
    }.run(true);
  }
  
  /**
   * Ask that the tests run stop before starting the next test. Phrased politely
   * because the test currently running will not be interrupted. It seems a
   * little odd to put this functionality here, but the <code>RunNotifier</code>
   * is the only object guaranteed to be shared amongst the many runners
   * involved.
   */
  public void pleaseStop() {
    stopRequested = true;
  }
}
