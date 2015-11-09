package com.carrotsearch.examples.randomizedrunner;

import org.junit.Before;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.TestCaseInstanceProvider;
import com.carrotsearch.randomizedtesting.annotations.TestCaseInstanceProvider.Type;
import com.carrotsearch.randomizedtesting.annotations.TestCaseOrdering;

/**
 * By default JUnit (and {@link RandomizedRunner}) creates a new class instance
 * for every test case it executes. This behavior makes sense to keep inter-test
 * dependencies to a minimum, but sometimes it's just plain inconvenient and
 * difficult to emulate (with static rules or static fields).
 * 
 * The {@link TestCaseInstanceProvider} permits changing the default behavior so
 * that each test method is executed on <b>the same</b> instance of the test
 * suite class.
 * 
 * The order of test case execution is still shuffled (unless overridden with
 * {@link TestCaseOrdering} so ideally the instance variables should be
 * stateless.
 *
 * Note that certain caveats apply, see the documentation of
 * {@link TestCaseInstanceProvider} for details.
 */
@TestCaseInstanceProvider(Type.INSTANCE_PER_CONSTRUCTOR_ARGS)
public class Test018TestCaseInstanceProviders extends RandomizedTest {
  /**
   * For the sake of the example only, we'll increment the instance's counter by
   * one for each test, printing the output to the console.
   */
  public int counter;
  
  /**
   * An instance context requiring some "costly" initialization. Note we could
   * just add the initialization here, but {@link Before} is nicer because it's
   * executed in the context of parent JUnit rules, etc.
   */
  public volatile Object costly;
  
  @Before
  public void costlySetup() throws InterruptedException {
    // Initialize once.
    if (costly == null) {
      Thread.sleep(1000);
      costly = new Object();
    }
  }
  
  @Test
  @Repeat(iterations = 3)
  public void addOne() {
    System.out.println("counter=" + (++counter) + ", costly=" + costly);
  }
}
