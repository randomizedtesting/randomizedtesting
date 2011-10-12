package com.carrotsearch.randomizedtesting;

import java.util.Random;

/**
 * Per-thread, per-lifecycle state randomness defined as an initial seed and 
 * the current Random instance.
 * 
 * <p>An instance of this class will be typically available from {@link RandomizedContext}.
 * No need to instantiate manually.
 * 
 * @see RandomizedContext
 */
public final class Randomness {
  final long seed;
  final Random random;

  public Randomness(long seed) {
    this.seed = seed;
    this.random = new RandomNoSetSeed(new Random(seed));
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
    StringBuilder b = new StringBuilder();
    b.append("[");
    for (int i = 0; i < randomnesses.length; i++) {
      if (i > 0) b.append(":");
      b.append(formatSeed(randomnesses[i].seed));
    }
    b.append("]");
    return b.toString();
  }

  /**
   * Parse a seed chain formatted with {@link #formatSeedChain(Randomness...)}. 
   */
  static long [] parseSeedChain(String chain) {
    chain = chain.replaceAll("[\\[\\]]", "");
    if (!chain.matches("[\\-0-9A-Za-z\\:]+")) {
      throw new IllegalArgumentException("Not a valid seed chain: " + chain);
    }
    String [] splits = chain.split("[\\:]");
    long [] longs = new long [splits.length];
    for (int i = 0; i < splits.length; i++)
      longs[i] = parseSeed(splits[i]);
    return longs;
  }

  private final static char [] HEX = "0123456789ABCDEF".toCharArray(); 

  /** Parse a single seed. */
  static long parseSeed(String seed) {
    long result = 0;
    for (char chr : seed.toCharArray()) {
      chr = Character.toLowerCase(chr);
      result = result << 4;
      if (chr >= '0' && chr <= '9')
        result |= (chr - '0');
      else if (chr >= 'a' && chr <= 'f')
        result |= (chr - 'a' + 10);
      else
        throw new RuntimeException("Expected hexadecimal seed: " + seed);
    }
    return result;
  }

  /** Format a single seed. */
  static String formatSeed(long seed) {
    StringBuilder b = new StringBuilder();
    do {
      b.append(HEX[(int) (seed & 0xF)]);
      seed = seed >>> 4;
    } while (seed != 0);
    return b.reverse().toString();
  }
}