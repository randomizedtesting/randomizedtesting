package com.carrotsearch.ant.tasks.junit4.spikes;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Seed;

public class FloatSampling extends RandomizedTest {
  @SuppressWarnings("serial")
  @Test
  @Seed("deadbeef")
  public void recurse() {
    Random rnd = new Random(1) {
      ArrayDeque<Float> r = new ArrayDeque<>(Arrays.asList(
          Math.nextAfter(1f, Double.NEGATIVE_INFINITY),
          Math.nextAfter(1f, Double.NEGATIVE_INFINITY),
          Math.nextAfter(1f, Double.NEGATIVE_INFINITY),
          Math.nextAfter(1f, Double.NEGATIVE_INFINITY)
          ));

      @Override
      public float nextFloat() {
        if (r.isEmpty()) {
          return super.nextFloat();
        } else {
          return r.removeFirst();
        }
      };
    };

    float from = -Float.MAX_VALUE, 
          to   = Float.MAX_VALUE;
    System.out.println("R=" + info(randomBetween(rnd, from, to)));
    
    System.out.println(info(-0.0f));
    System.out.println(info(Math.nextAfter(0f, Double.NEGATIVE_INFINITY)));
    System.out.println(info(Math.nextAfter(-0.0f, Double.NEGATIVE_INFINITY)));
  }

  public static float randomBetween(Random rnd, float from, float to) {
    assert !Float.isNaN(from) &&
           !Float.isNaN(to) &&
           !Float.isInfinite(from) &&
           !Float.isInfinite(to);

    float span = Float.MAX_VALUE;
    // TODO: when there's a difference of 4 ulps low and high may never converge, we should
    // select manually then.
    while (from != to) {
      // proportional selection factor between from and to.
      float s = rnd.nextFloat();

      // Determine lower ends of an interval where s and nextUp(s) would divide
      // the representation space between from and to. Then proceed recursively
      // until there's a single element left.
      float low = low(s, from, to);
      float high = low(Math.nextUp(s), from, to);

      // Sanity check.
      assert (span = alwaysDecreases(span, high - low)) >= 0;
      System.out.println(info(low));
      System.out.println(info(high));
      System.out.println("(" + from + ", " + to + ") -> s=" + s + " => (" + low + "," + high + "), range=" + (high - low));

      from = low; to = high;
    }

    return from;
  }

  private static float alwaysDecreases(float prevSpan, float newSpan) {
    if (prevSpan <= newSpan) {
      throw new AssertionError("Interval should always decrease: " + prevSpan + " " + newSpan);
    }
    return newSpan;
  }

  private static float low(float s, float min, float max) {
    // Avoid range overflow by calculating proportion differently:
    // s * (max -  min) + min = 
    // s * max + (1 - s) * min 

    if (s == 0f) return min;
    if (s == 1f) return max;

    // All intermediate results are always pessimistically rounded down.

    float c1 = s * max;
    if (c1 > 0) {
      c1 = Math.nextAfter(c1, Double.NEGATIVE_INFINITY);
    }

    float c4 = 1f - s;
    if (c4 > 0) {
      c4 = Math.nextAfter(c4, Double.NEGATIVE_INFINITY);
    }
    
    float c2 = c4 * min;
    if (c2 > 0) {
      c2 = Math.nextAfter(c2, Double.NEGATIVE_INFINITY);
    }

    float c3 = c1 + c2;
    if (c3 > 0) {
      c3 = Math.nextAfter(c3, Double.NEGATIVE_INFINITY);
    }

    return c3;
  }

  private static String info(float f) {
    return String.format(Locale.ROOT, "<%.50f, 0x%08x>", f, Float.floatToIntBits(f));
  }
}
