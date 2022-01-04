package com.carrotsearch.randomizedtesting.extensions;

public class RandomSeed {
  private static final char[] HEX = "0123456789ABCDEF".toCharArray();

  public final long value;

  public RandomSeed(long seed) {
    this.value = seed;
  }

  @Override
  public String toString() {
    long seed = value;
    StringBuilder b = new StringBuilder();
    do {
      b.append(HEX[(int) (seed & 0xF)]);
      seed = seed >>> 4;
    } while (seed != 0);
    return b.reverse().toString();
  }
}
