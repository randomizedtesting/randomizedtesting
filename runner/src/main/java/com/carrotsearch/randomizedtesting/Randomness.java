package com.carrotsearch.randomizedtesting;

import java.util.Random;

/**
 * Random seed and current random state. Per-thread.
 */
public final class Randomness {
  final long seed;
  final Random random;

  public Randomness(long seed) {
    this.seed = seed;
    this.random = new Random();
  }

  /** Random instance for this randomness. */
  public Random getRandom() {
    return random;
  }  

  /** Starting seed, read-only. */
  public long getSeed() {
    return seed;
  }

  @Override
  public String toString() {
    return "[Randomness, seed=" + formatSeedChain(this) + "]";
  }

  /**
   * Formats randomness seed or seeds into something the user can type in to get predictably repeatable
   * execution.
   */
  static String formatSeedChain(Randomness... randomnesses) {
    // TODO: use base64-like encoding to make them shorter and get rid of the '-' character.
    StringBuilder b = new StringBuilder();
    b.append("[");
    for (int i = 0; i < randomnesses.length; i++) {
      if (i > 0) b.append(":");
      b.append(Long.toString(randomnesses[i].seed, 16));
    }
    b.append("]");
    return b.toString();
  }

  /**
   * Parse a seed chain formatted with {@link #formatSeedChain(Randomness...)}. 
   */
  static long [] parseSeedChain(String chain) {
    if (!chain.matches("[0-9A-Za-z\\:]+")) {
      throw new IllegalArgumentException("Not a valid seed chain: " + chain);
    }
    String [] splits = chain.split("[\\:]");
    long [] longs = new long [splits.length];
    for (int i = 0; i < splits.length; i++)
      longs[i] = Long.parseLong(splits[i], 16);
    return longs;
  }
}