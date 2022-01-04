package com.carrotsearch.randomizedtesting.extensions;

import java.util.List;

public class InitialSeed {
  public static List<RandomSeed> compute() {
    final String propertySeed = System.getProperties().getProperty(SysProps.TESTS_SEED);
    if (propertySeed != null) {
      return SeedChain.parse(propertySeed);
    } else {
      return List.of(SeedChain.WILDCARD);
    }
  }
}
