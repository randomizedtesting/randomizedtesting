package com.carrotsearch.examples.randomizedrunner;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Timeout;

/**
 * {@link RandomizedRunner} has built-in support for enforcing test timeouts. If
 * a given test fails to execute in the given deadline, its thread will be
 * forcibly terminated (details below) and the test case will end in a failure.
 * 
 * <p>
 * A timeout can be specified in two ways. First, the standard JUnit's
 * {@link Test#timeout()} attribute can be used (see
 * {@link #standardAnnotation()}). Alternatively, a more specific @link Timeout}
 * annotation is also provided if one needs to be explicit.
 * 
 * <p>
 * The termination of a test thread is done in several steps ranking from subtle
 * to brute-force:
 * <ul>
 * <li>First, the thread is interrupted a few times with
 * {@link Thread#interrupt()}. In many situations (I/O wait, waiting on a
 * monitor) this will be enough to stop the test case and return.
 * <li>If interrupt doesn't work (busy-loops, interrupted exception caught and
 * ignored), an attempt is made to stop the thread using {@link Thread#stop()}.
 * This should cause the test thread to throw {@link ThreadDeath} exception at
 * the current execution pointer and propagate up the stack.
 * <li>Should {link {@link ThreadDeath} be caught and ignored as well, the
 * thread is declared a zombie and other tests execution is resumed with the
 * thread running in the background.
 * </ul>
 * 
 * <p>
 * The information about attempts to interrupt the thread are logged to the
 * Java's logging system along with the information about stack traces where the
 * thread resided when interrupts were sent to it. This is typically useful in
 * diagnosing what the thread was doing and why it couldn't be terminated. Keeping
 * Java logging system enabled is thus strongly encouraged.
 */
public class Test008Timeouts extends RandomizedTest {
  @Test(timeout = 500)
  public void standardAnnotation() {
    sleep(10000);
  }

  @Test
  @Timeout(millis = 500)
  public void timeoutAnnotation() {
    sleep(10000);
  }
}
