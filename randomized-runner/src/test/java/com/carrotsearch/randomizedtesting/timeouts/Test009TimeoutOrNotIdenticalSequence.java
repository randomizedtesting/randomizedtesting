package com.carrotsearch.randomizedtesting.timeouts;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.Timeout;

/**
 * It should not matter for the random sequence whether {@link Timeout} is
 * used or not.
 */
public class Test009TimeoutOrNotIdenticalSequence extends WithNestedTestClass {
  final static ArrayList<String> seeds = new ArrayList<String>();

  public static class Nested1 extends RandomizedTest {
    @Test
    @Seed("deadbeef")
    @Repeat(iterations = 2, useConstantSeed = false)
    public void testNoTimeout() {
      assumeRunningNested();
      seeds.add(randomAsciiLettersOfLength(20));
    }
  }
  
  public static class Nested2 extends Nested1 {
    @Override
    @Test
    @Seed("deadbeef")
    @Repeat(iterations = 2, useConstantSeed = false)
    @Timeout(millis = 30 * 1000)
    public void testNoTimeout() {
      super.testNoTimeout();
    }
  }

  @Timeout(millis = 30 * 1000)
  public static class Nested3 extends Nested1 {
    @Override
    @Test
    @Seed("deadbeef")
    @Repeat(iterations = 2, useConstantSeed = false)
    @Timeout(millis = 30 * 1000)
    public void testNoTimeout() {
      super.testNoTimeout();
    }
  }

  @Test
  public void checkAllRunsIdentical() {
    List<String> previous = null;
    for (Class<?> c : new Class<?> [] {Nested1.class, Nested2.class, Nested3.class}) {
      seeds.clear();
      Assertions.assertThat(runTests(c).wasSuccessful()).isTrue();

      if (previous != null) {
        Assertions.assertThat(seeds).as("Class " + c.getSimpleName()).isEqualTo(previous);
      } else {
        previous = new ArrayList<>(seeds); 
      }
    }
  }
}
