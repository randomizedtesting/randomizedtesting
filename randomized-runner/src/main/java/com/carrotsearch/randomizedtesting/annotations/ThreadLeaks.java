package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Timer;
import java.util.concurrent.Executors;

/**
 * A scope (test suite, test case) must not leave active threads behind. Should
 * such a situation occur, the offending threads will be first interrupted 
 * ({@link Thread#interrupt()}), then stopped ({@link Thread#stop()}). If this still
 * doesn't terminate the offending thread, a message is logged and the tests 
 * <b>will</b> continue even though background activity can interfere with them. 
 * 
 * <p>A test case leaking threads will end in a failure (by default). 
 */
@SuppressWarnings("all")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface ThreadLeaks {
  /**
   * Time in millis to "linger" for any left-behind threads. If equals 0, there is no
   * waiting. 
   * 
   * <p>This is particularly useful
   * if there's no way to join the threads from test-case level (as with {@link Timer}
   * or {@link Executors}).</p>
   */
  int linger() default 0;

  /**
   * The number of "probes" of the offending thread's stack trace before an attempt
   * is made to kill it. Snapshots are taken at random intervals between 10 and 100 
   * milliseconds each and dumped to system logger to facilitate debugging of the offending thread.
   * 
   * <p>Setting this value to 0 means no samples will be taken. 
   */
  int stackSamples() default 10;

  /**
   * Should left-behind threads cause unit test (or suite-level) failures? If this flag
   * is <code>false</code> the test will not be marked as failed even if it left threads
   * behind. Also, no leaks will be reported. Use with caution.
   */
  boolean failTestIfLeaking() default true;
  
  /**
   * Instructs the test that any detected left-over threads belong to the suite scope
   * instead of the test scope. This is occasionally useful, for example if an executor
   * is allocated for suite scope, but threads are allocated during individual tests.  
   */
  boolean leakedThreadsBelongToSuite() default false;
}