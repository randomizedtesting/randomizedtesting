package com.carrotsearch.randomizedtesting.api;

import com.carrotsearch.randomizedtesting.extensions.RandomSeed;
import com.carrotsearch.randomizedtesting.extensions.SeedChain;
import com.carrotsearch.randomizedtesting.internal.Threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.function.LongUnaryOperator;

public class RandomizedContext {
  private final Thread owner;
  private final RandomSeed seed;
  private final RandomizedContext parent;
  private final Random random;

  public RandomizedContext(Thread owner, RandomSeed seed) {
    this(null, owner, seed);
  }

  private RandomizedContext(RandomizedContext parent, Thread owner, RandomSeed seed) {
    this.owner = owner;
    this.seed = seed;
    this.random = new Random(seed.value);
    this.parent = parent;
  }

  @Override
  public String toString() {
    return "Randomized context ["
        + ("seedChain=" + getSeedChain())
        + ","
        + ("thread=" + Threads.threadName(owner))
        + "]";
  }

  public SeedChain getSeedChain() {
    ArrayList<RandomSeed> seeds = new ArrayList<>();
    for (RandomizedContext c = this; c != null; c = c.getParent()) {
      seeds.add(c.seed);
    }
    Collections.reverse(seeds);
    return new SeedChain(seeds);
  }

  private RandomizedContext getParent() {
    return parent;
  }

  public RandomizedContext fork(Thread forThread, LongUnaryOperator initialSeedFunction) {
    long initSeed = initialSeedFunction.applyAsLong(getRandom().nextLong());
    return new RandomizedContext(this, forThread, new RandomSeed(initSeed));
  }

  public Random getRandom() {
    if (Thread.currentThread() != owner) {
      throw new RuntimeException(
          String.format(
              Locale.ROOT,
              "%s instance is bound to thread %s, can't access it from thread: %s",
              RandomizedContext.class.getName(),
              owner,
              Thread.currentThread()));
    }
    return random;
  }
}
