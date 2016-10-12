package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.TestContextRandomSupplier;

/**
 * 
 */
@TestContextRandomSupplier(TestCustomTestContextSupplier.CustomSupplier.class)
public class TestCustomTestContextSupplier extends RandomizedTest {
  public static class CustomSupplier implements RandomSupplier {
    @SuppressWarnings("serial")
    @Override
    public Random get(long seed) {
      return new Random() {
        int value;

        @Override
        public int nextInt() {
          return value++;
        }
      };
    }
  }

  @Test
  public void testCustomSupplier() throws Exception {
    // Can't just check for instanceof because random can be wrapped
    // with AssertingRandom.
    Random rnd = RandomizedContext.current().getRandom();
    for (int i = 0; i < 10; i++) {
      assertEquals(rnd.nextInt(), i);
    }
  }
}
