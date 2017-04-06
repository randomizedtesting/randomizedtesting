package com.carrotsearch.randomizedtesting;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.SeedDecorators;
import com.carrotsearch.randomizedtesting.generators.RandomStrings;

/**
 * Test seed decorators.
 */
public class TestSeedDecorator extends WithNestedTestClass {
  private static List<String> runnerSeeds = new ArrayList<String>();
  private static List<String> strings = new ArrayList<String>();
  
  @RunWith(RandomizedRunner.class)
  public static class Nested1 {
    @BeforeClass
    public static void generateSequence() {
      strings.add(RandomStrings.randomAsciiLettersOfLength(RandomizedContext.current().getRandom(), 200));
    }

    @Test
    public void method1() {
      assumeRunningNested();
      runnerSeeds.add(RandomizedContext.current().getRunnerSeedAsString());
    }
  }

  public static class Nested2 extends Nested1 {
  }

  @SeedDecorators({
    MixWithSuiteName.class
  })
  public static class Nested3 extends Nested1 {
  }

  public static class Nested4 extends Nested3 {
  }

  @Before @After
  public void cleanup() {
    runnerSeeds.clear();
    strings.clear();
    System.clearProperty(SysGlobals.SYSPROP_RANDOM_SEED());
  }

  @Test
  public void testDecoratedMaster() {
    String masterSeed = SeedUtils.formatSeed(new Random().nextLong());
    System.setProperty(SysGlobals.SYSPROP_RANDOM_SEED(), masterSeed);

    // These classes should get a different master seed (perturbed by decorator).
    runTests(Nested1.class, Nested2.class, Nested3.class, Nested4.class);

    // All four classes get the same initial "runner" seed.
    Assert.assertEquals(4, runnerSeeds.size());
    Assert.assertEquals(1, new HashSet<String>(runnerSeeds).toArray().length);

    // @BeforeClass scope strings for Nested1 and Nested2 should be the same
    // because these classes share identical master seed.
    Assertions.assertThat(strings.get(1)).isEqualTo(strings.get(0));
    // but Nested3 and Nested4 have a seed decorator so strings there
    // should be different.
    Assertions.assertThat(strings.get(2)).isNotEqualTo(strings.get(0));
    Assertions.assertThat(strings.get(3)).isNotEqualTo(strings.get(0));
    Assertions.assertThat(strings.get(2)).isNotEqualTo(strings.get(3));
  }
}
