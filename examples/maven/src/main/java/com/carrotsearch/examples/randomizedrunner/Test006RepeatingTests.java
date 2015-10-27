package com.carrotsearch.examples.randomizedrunner;

import org.junit.*;

import com.carrotsearch.randomizedtesting.*;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;

import static org.junit.Assert.*;

/**
 * In example {@link Test005RecoveringRandomSeed} we presented
 * {@link Randomness} contexts and how they are derived from a master seed. Once
 * you know a certain test case fails it is usually beneficial to immediately
 * check if it <b>always fails</b> on a given seed (which means there is a
 * deterministic failure scenario). A simple way to do so would be to re-run a
 * test case a few times. The same effect can be achieved by adding a
 * {@link Repeat} annotation with {@link Repeat#useConstantSeed()} set to
 * <code>false</code> attribute as shown in the method {@link #repeatFailure()}
 * below. 
 * <pre>
 * {@literal @}{@link Repeat}(iterations = 5, useConstantSeed = true)
 * {@literal @}{@link Seed}("f00ddead")
 * {@literal @}{@link Test}
 * public void repeatFailure() { //...
 * </pre>
 * Note how the seed is fixed using {@link Seed} annotation (on the
 * method) rather than on the master. This ensures the method's context is
 * pinned to that value, but the master is still random. If you have
 * {@link BeforeClass} hooks that depend on randomness you should use
 * suite-level {@link Seed} annotation and pin the master seed instead.
 * 
 * <p>You can also set {@link Repeat#useConstantSeed()} to <code>false</code> and
 * then every iteration of the test method will have a pseudo-random context derived
 * from the first one (pinned or not). This can be useful to tell how frequently
 * a given test case fails for a random seed. For {@link #halfAndHalf()} method
 * about 50% of iterations will fail.  
 */
public class Test006RepeatingTests extends RandomizedTest {
  @Repeat(iterations = 5, useConstantSeed = true)
  @Seed("f00ddead")
  @Test
  public void repeatFailure() {
    assertTrue(randomBoolean());
  }

  @Repeat(iterations = 10, useConstantSeed = false)
  @Test
  public void halfAndHalf() {
    assertTrue(randomBoolean());
  }
}
