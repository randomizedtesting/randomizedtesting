package com.carrotsearch.randomizedtesting;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.Repeat;

@Repeat(iterations = 100)
public class TestRandom extends RandomizedTest {
  @Test
  public void testDoubleDistribution() {
    Random rnd = getRandom();

    int [] buckets = new int [100];
    int reps = 1000000;
    for (int i = 0; i < reps; i++) {
      double d = rnd.nextDouble();
      if (d < 0d || d >= 1d) {
        fail("Oops: " + d);
      }
      
      buckets[(int) (d * buckets.length)]++;
    }

    // The distribution should be +- 10% within average.
    int expectedAverage = reps / buckets.length;
    int min = expectedAverage - expectedAverage / 10;
    int max = expectedAverage + expectedAverage / 10;
    for (int i = 0; i < buckets.length; i++) {
      if (buckets[i] < min || buckets[i] > max) {
        fail("oops: " + buckets[i] + " biased? " + Arrays.toString(buckets));
      }
    }
  }
  
  @Test
  public void testFloatDistribution() {
    Random rnd = getRandom();

    int [] buckets = new int [100];
    int reps = 1000000;
    for (int i = 0; i < reps; i++) {
      float d = rnd.nextFloat();
      if (d < 0f || d >= 1f) {
        fail("Oops: " + d);
      }
      
      buckets[(int) (d * buckets.length)]++;
    }

    // The distribution should be +- 10% within average.
    int expectedAverage = reps / buckets.length;
    int min = expectedAverage - expectedAverage / 10;
    int max = expectedAverage + expectedAverage / 10;
    for (int i = 0; i < buckets.length; i++) {
      if (buckets[i] < min || buckets[i] > max) {
        fail("oops: " + buckets[i] + " biased? " + Arrays.toString(buckets));
      }
    }
  }
}
