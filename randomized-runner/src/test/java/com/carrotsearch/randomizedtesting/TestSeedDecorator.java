package com.carrotsearch.randomizedtesting;

import java.util.*;

import org.junit.*;
import org.junit.runner.JUnitCore;
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
      strings.add(
          RandomStrings.randomAsciiOfLength(RandomizedContext.current().getRandom(), 200));
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

    // These should get a different seed (perturbed by decorator).
    JUnitCore.runClasses(Nested1.class, Nested2.class, Nested3.class, Nested4.class);

    Assert.assertEquals(4, runnerSeeds.size());
    Assert.assertEquals(1, new HashSet<String>(runnerSeeds).toArray().length);
    
    // We should also check strings generated in @BeforeClass.
    Assert.assertEquals(strings.get(0), strings.get(1));
    Assert.assertFalse(strings.get(0).equals(strings.get(2)));
    Assert.assertFalse(strings.get(0).equals(strings.get(3)));
    Assert.assertFalse(strings.get(2).equals(strings.get(3)));
  }
}
