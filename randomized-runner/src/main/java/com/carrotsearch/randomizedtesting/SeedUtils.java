package com.carrotsearch.randomizedtesting;

/**
 * Utilities for parsing random seeds.
 */
public final class SeedUtils {
  private final static char [] HEX = "0123456789ABCDEF".toCharArray();

  private SeedUtils() {}

  /** 
   * Parse a single seed. The seed needs to be cleaned up from any surrounding characters. 
   */
  public static long parseSeed(String seed) {
    long result = 0;
    for (char chr : seed.toCharArray()) {
      chr = Character.toLowerCase(chr);
      result = result << 4;
      if (chr >= '0' && chr <= '9')
        result |= (chr - '0');
      else if (chr >= 'a' && chr <= 'f')
        result |= (chr - 'a' + 10);
      else
        throw new IllegalArgumentException("Expected hexadecimal seed: " + seed);
    }
    return result;
  }

  /** 
   * Format a single seed. 
   */
  public static String formatSeed(long seed) {
    StringBuilder b = new StringBuilder();
    do {
      b.append(HEX[(int) (seed & 0xF)]);
      seed = seed >>> 4;
    } while (seed != 0);
    return b.reverse().toString();
  }

  /**
   * Parse a seed chain formatted with {@link SeedUtils#formatSeedChain(Randomness...)}. 
   */
  public static long [] parseSeedChain(String chain) {
    chain = chain.replaceAll("[\\[\\]]", "");
    if (!chain.matches("[0-9A-Fa-f\\:]+")) {
      throw new IllegalArgumentException("Not a valid seed chain: " + chain);
    }
    String [] splits = chain.split("[\\:]");
    long [] longs = new long [splits.length];
    for (int i = 0; i < splits.length; i++)
      longs[i] = parseSeed(splits[i]);
    return longs;
  }

  /**
   * Formats randomness seed or seeds into something the user can type in to get predictably repeatable
   * execution.
   */
  public static String formatSeedChain(Randomness... randomnesses) {
    StringBuilder b = new StringBuilder();
    b.append("[");
    for (int i = 0; i < randomnesses.length; i++) {
      if (i > 0) b.append(":");
      b.append(formatSeed(randomnesses[i].getSeed()));
    }
    b.append("]");
    return b.toString();
  }
}
