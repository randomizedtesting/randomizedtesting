package com.carrotsearch.randomizedtesting;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.google.common.base.Strings;

public class TestAtLeastAtMostHistograms {
  public static int[] picks;
  public static int min;
  public static int max;
  public static int reps;

  public static abstract class Histograms extends RandomizedTest {
    @Test
    public void doTest() {
      for (int i = 0; i < reps; i++) {
        int pick = doSomething();
        int bucket = (pick - min) * picks.length / (max - min + 1);
        picks[bucket]++;
      }
    }

    protected abstract int doSomething();
  }

  public static class AtLeast extends Histograms {
    @Override
    protected int doSomething() {
      return atLeast(min);
    }
  }

  public static class ScaledValue extends Histograms {
    @Override
    protected int doSomething() {
      return scaledRandomIntBetween(min, max);
    }
  }

  @Test
  public void testAtLeast() {
    int buckets = 10;
    picks = new int [buckets];
    min = 10000;
    max = min * 3;
    reps = 1000000;

    for (double multiplier : new double [] {0, 0.5, 1, 1.5, 2, 3, 4}) {
      System.out.println("-- multiplier: " + multiplier);
      System.setProperty(RandomizedTest.SYSPROP_MULTIPLIER, Double.toString(multiplier));

      Result r = JUnitCore.runClasses(ScaledValue.class);
      for (Failure f : r.getFailures()) {
        System.out.println(f.getTrace());
      }
      Assert.assertTrue(r.wasSuccessful());
  
      dumpHistogram();
    }
  }

  private void dumpHistogram() {
    int maxValue = picks[0];
    for (int v : picks) maxValue = Math.max(maxValue, v);
    
    for (int i = 0; i < picks.length; i++) {
      System.out.println(String.format(Locale.ENGLISH,
          "%8d: %s",
          min + (max - min) * i / (picks.length - 1),
          Strings.repeat("#", picks[i] * 80 / maxValue)));
    }
  }
}
