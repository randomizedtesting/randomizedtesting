package com.carrotsearch.examples.randomizedrunner;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/**
 * To start using pseudo-randomization we need to get hold of the
 * {@link RandomizedContext} instance associated with the test. This can be done
 * manually, as shown in {@link #getContextByHand()} method or (better) we can
 * extend {@link RandomizedTest} and have a superclass method to handle this
 * (and more) for us as shown in {#link #getContextFromSuper()}
 * 
 * <p>Note that {@link Random} instances acquired from the context are 
 * preinitialized with a repeatable seed (we'll get to that) so tests
 * can be re-run with the same random sequence if something fails.
 */
public class Test002ExtendingRandomizedTest extends RandomizedTest {
  @Test
  public void getContextByHand() {
    RandomizedContext context = RandomizedContext.current();
    Random rnd = context.getRandom();
    System.out.println("Random, next int: " + rnd.nextInt());
  }

  @Test
  public void getContextFromSuper() {
    Random rnd = super.getRandom();
    System.out.println("Random, next int: " + rnd.nextInt());
  }
}
