package com.carrotsearch.randomizedtesting.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SeedChain {
  static final RandomSeed WILDCARD = new RandomSeed(0L);

  private final ArrayList<RandomSeed> seeds;

  public SeedChain(ArrayList<RandomSeed> seeds) {
    this.seeds = seeds;
  }

  public static List<RandomSeed> parse(String chain) {
    chain = chain.replaceAll("[\\[\\]]", "");

    String[] splits = chain.split("[:]");

    RandomSeed[] seeds = new RandomSeed[splits.length];
    for (int i = 0; i < splits.length; i++) {
      if (!splits[i].matches("[0-9A-Fa-f]+|[*]")) {
        throw new IllegalArgumentException(
            "Invalid component \"" + splits[i] + "\" in seed chain: " + chain);
      }
      seeds[i] = parseSeed(splits[i]);
    }
    return Arrays.asList(seeds);
  }

  private static RandomSeed parseSeed(String component) {
    if (component.equals("*")) {
      return WILDCARD;
    } else {
      long result = 0;
      for (char chr : component.toCharArray()) {
        chr = Character.toLowerCase(chr);
        result = result << 4;
        if (chr >= '0' && chr <= '9') result |= (chr - '0');
        else if (chr >= 'a' && chr <= 'f') result |= (chr - 'a' + 10);
        else throw new IllegalArgumentException("Expected hexadecimal seed: " + component);
      }
      return new RandomSeed(result);
    }
  }

  @Override
  public String toString() {
    return "["
        + seeds.stream()
            .map(v -> v == WILDCARD ? "*" : v.toString())
            .collect(Collectors.joining(":"))
        + "]";
  }
}
