package com.carrotsearch.randomizedtesting;

import java.util.Random;

public interface RandomSupplier {
  static RandomSupplier DEFAULT = new RandomSupplier() {
    @Override
    public Random get(long seed) {
      return new Xoroshiro128PlusRandom(seed);
    }
  };

  Random get(long seed);
}
