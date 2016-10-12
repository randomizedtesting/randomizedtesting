package com.carrotsearch.randomizedtesting;

import java.util.Random;

public interface RandomSupplier {
  static RandomSupplier DEFAULT = new RandomSupplier() {
    @Override
    public Random get(long seed) {
      return new Random(seed);
    }
  };

  Random get(long seed);
}
