package com.carrotsearch.examples.randomizedrunner;

import java.util.Random;

import org.junit.*;

import com.carrotsearch.randomizedtesting.*;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;

/**
 * {@link RandomizedRunner} uses several "contexts", each of which is assigned a
 * predictable {@link Random} and is modeled using a {@link Randomness}
 * instance. The "suite" or "master" context is available from
 * {@link BeforeClass} or {@link AfterClass} hooks, for example. Each test
 * method has a nested context with a random seed derived from the master. This
 * way even though the order of tests is shuffled and each test can make a
 * random number of calls to its own context's {@link Random} instance, the
 * global execution paths can always be repeated from the same master seed. The
 * question is: how do we know what master seed was used? There are at least two
 * ways to find out.
 * 
 * <p>The master seed is always available from
 * {@link RandomizedContext#getRunnerSeedAsString()} so one can simply print it to the
 * console. The current context's {@link Randomness} itself can be printed to the
 * console. In two methods in this class {@link #printMasterContext()} and {@link #printContext()} 
 * we print the master seed and current context's {@link Randomness}, note how the static 
 * context's {@link Randomness} is identical with the runner's but the test context 
 * is a derived value. 
 * <pre>
 * # Static context ({@literal @}BeforeClass)
 * AF567B2B9F8A8F1C
 * [Randomness, seed=[AF567B2B9F8A8F1C]]
 * # Test context ({@literal @}Test)
 * AF567B2B9F8A8F1C
 * [Randomness, seed=[EE581D5EC61D6BCF]]
 * </pre>
 * In {@link Test006RepeatingTests} we will see how this derived
 * seed is used with {@link Repeat} annotation.
 * 
 * <p>Normally we will not be interested in a random seed if a test case passes. But if a test
 * case fails we will want to know the seed to be able to repeat the test. {@link RandomizedRunner}
 * augments the stack trace of all exceptions that cross the context boundary (this includes
 * assertion errors, assumption failures and any other exceptions). In method {@link #failure()}
 * we demonstrate this by failing on a constant condition. If you run this test suite, you'll note
 * the stack trace of the failing method to be something like this:
 * <pre>
 * java.lang.AssertionError
 *   at __randomizedtesting.SeedInfo.seed([AF567B2B9F8A8F1C:44E2D1A039274F2A]:0)
 *   at org.junit.Assert.fail(Assert.java:92)
 * </pre>
 * 
 * The first line of the stack trace is a synthetic (non-existing) class with "source file"
 * entry containing all contexts' seeds on the stack (from master to the current test method).
 * In this case, you can see the master context first (<tt>AF567B2B9F8A8F1C</tt>), followed
 * by the test's context (<tt>44E2D1A039274F2A</tt>). The entire class has a fixed master seed
 * so that the result will always be the same here:
 * <pre>
 * {@literal @}{@link Seed}("AF567B2B9F8A8F1C")
 * public class Test005RecoveringRandomSeed extends RandomizedTest { // ...
 * </pre>
 */
@Seed("AF567B2B9F8A8F1C")
public class Test005RecoveringRandomSeed extends RandomizedTest {
  @BeforeClass
  public static void printMasterContext() {
    System.out.println("# Static context (@BeforeClass)");
    System.out.println(getContext().getRunnerSeedAsString());
    System.out.println(RandomizedContext.current().getRandomness());
  }

  @Test
  public void printContext() {
    System.out.println("# Test context (@Test)");
    System.out.println(getContext().getRunnerSeedAsString());
    System.out.println(RandomizedContext.current().getRandomness());
  }

  @Test
  public void failure() {
    Assert.assertTrue(false);
  }
}
