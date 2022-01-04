package com.carrotsearch.randomizedtesting.extensions;

import com.carrotsearch.randomizedtesting.api.RandomizedContext;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ExtensionExecutionContext {
  /**
   * A sequencer for affecting the initial seed in case of rapid succession of this class instance
   * creations. Not likely, but can happen two could get the same seed.
   */
  private static final AtomicLong sequencer = new AtomicLong();

  private final List<RandomSeed> seedList;
  private final ArrayDeque<Map.Entry<String, RandomizedContext>> contextStack = new ArrayDeque<>();

  public ExtensionExecutionContext(List<RandomSeed> seedList) {
    assert seedList.size() >= 1;
    this.seedList = seedList;
  }

  public void pop(ExtensionContext ctx) {
    var key = getKey(ctx);
    if (contextStack.isEmpty() || !contextStack.peekLast().getKey().equals(key)) {
      throw new AssertionError("No context or incorrect context for key: " + key);
    }
    contextStack.removeLast();
  }

  private String getKey(ExtensionContext ctx) {
    return ctx.getUniqueId();
  }

  public RandomizedContext getContext(ExtensionContext ctx) {
    var key = getKey(ctx);
    if (contextStack.isEmpty() || !contextStack.peekLast().getKey().equals(key)) {
      throw new AssertionError("No context or incorrect context for key: " + key);
    }
    return contextStack.peekLast().getValue();
  }

  public void push(ExtensionContext extensionContext) {
    RandomizedContext ctx;
    var forThread = Thread.currentThread();
    var key = getKey(extensionContext);

    if (contextStack.isEmpty()) {
      ctx =
          new RandomizedContext(
              forThread, Objects.requireNonNull(seedAtLevel(extensionContext, 0)));
    } else {
      var forcedSeed = seedAtLevel(extensionContext, contextStack.size());
      var last = contextStack.peekLast();

      ctx =
          last.getValue()
              .fork(
                  forThread,
                  seed -> forcedSeed != null ? forcedSeed.value : seed ^ Hashing.longHash(key));
    }

    contextStack.addLast(Map.entry(key, ctx));
  }

  private RandomSeed seedAtLevel(ExtensionContext context, int index) {
    if (index >= seedList.size()) {
      return null;
    }
    var seed = seedList.get(index);
    if (seed == SeedChain.WILDCARD) {
      seed = new RandomSeed(Hashing.mix64(sequencer.incrementAndGet() + System.nanoTime()));
    }
    return seed;
  }
}
