package com.carrotsearch.randomizedtesting.examples;

import com.carrotsearch.randomizedtesting.api.Randomized;
import com.carrotsearch.randomizedtesting.api.RandomizedContext;
import com.carrotsearch.randomizedtesting.extensions.RandomizedContextProviderExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;

@ExtendWith({RandomizedContextProviderExtension.class})
public class Test001SimpleUseCase {

  @BeforeAll
  public static void beforeAll1(RandomizedContext context) {
    log("BeforeAll1", context);
  }

  private static void log(String prefix, RandomizedContext context) {
    System.out.printf(
        Locale.ROOT,
        "prefix: %15s, %016x %s%n",
        prefix,
        context.getRandom().nextLong(),
        context.getSeedChain());
  }

  @BeforeAll
  public static void beforeAll2(RandomizedContext context) {
    log("BeforeAll2", context);
  }

  public Test001SimpleUseCase() {
    //log("  Constructor", context);
  }

  @BeforeEach
  public void beforeEach(RandomizedContext context) {
    log("  BeforeEach", context);
  }

  @AfterEach
  public void afterEach(RandomizedContext context) {
    log("  AfterEach", context);
  }

  @Randomized
  @Test
  public void testMethod1(RandomizedContext context) {
    log("  Method1", context);
  }

  @Randomized
  @RepeatedTest(10)
  public void testMethod2(RandomizedContext context) {
    log("  Method2", context);
  }
}
