package com.carrotsearch.examples.randomizedrunner;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.SysGlobals;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Seeds;

/**
 * Randomness is entwined everywhere in {@link RandomizedRunner}. An instance of
 * {@link Random} is of course available from a {@link RandomizedContext}, but
 * even tests themselves are randomized, or to be more precise: their order of
 * execution is shuffled.
 * 
 * <p>
 * In this example we have two classes that contain three methods (they're nested
 * under a single suite class for simplicity). Every execution of class
 * {@link Test004MoreRandomness.OrderRandomized} will be different, shuffling
 * test methods around (and the random numbers written to the output). 
 * We can "pin" the execution order by forcing the master random
 * seed using {@link Seed} annotation on the class (or a system property
 * {@link SysGlobals#SYSPROP_RANDOM_SEED}). Doing so also fixes all derivative random
 * generators in all tests - this is shown in  
 * {@link Test004MoreRandomness.OrderRandomizedButFixed}, every execution of this
 * class will be identical (and will emit identical pseudo-random numbers).
 * 
 * <p>
 * All this is meant for one purpose: help in reproducing a failed randomized test
 * case. Once a test case fails, make sure you write down the random seed number
 * that caused the failure and add appropriate {@link Seeds} annotation on the method
 * that failed like so:
 * <pre>
 * {@literal @}{@link Seeds}({
 *   {@literal @}{@link Seed}("012345"),
 *   {@literal @}{@link Seed}()
 * })
 * </pre>
 * where 012345 is the replaced by the seed that caused the failure. This makes
 * the test methods run with a fixed seed once and then with a random seed again,
 * easily creating a new regression test so that the bug does not reoccur in the 
 * future. An example of that is shown in 
 * {@link Test004MoreRandomness.OrderRegression#regression()}. Also note
 * how {@link RandomizedRunner} modifies test method names for such "expanded" methods, appending
 * the random seed as a parameter. This is needed to avoid duplicate 
 * test {@link Description} objects (a design flaw in JUnit). We will see these parameters 
 * again in the example concerning parameterized tests.
 */
@RunWith(Suite.class)
@SuiteClasses({
  Test004MoreRandomness.OrderRandomized.class,
  Test004MoreRandomness.OrderRandomizedButFixed.class,
  Test004MoreRandomness.OrderRegression.class
})
public class Test004MoreRandomness {
  public static class OrderRandomized extends RandomizedTest {
    @Test public void method1() { System.out.println("method1, " + randomInt()); }
    @Test public void method2() { System.out.println("method2, " + randomInt()); }
    @Test public void method3() { System.out.println("method3, " + randomInt()); }

    @AfterClass 
    public static void empty() {
      System.out.println("--");
    }
  }

  @Seed("deadbeef")
  public static class OrderRandomizedButFixed extends OrderRandomized {
  }

  public static class OrderRegression extends RandomizedTest {
    @Seeds({
      @Seed("deadbeef"),
      @Seed()
    })
    @Test public void regression() { System.out.println("regression, " + randomInt()); }
  }  
}
