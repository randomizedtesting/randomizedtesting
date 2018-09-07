package com.carrotsearch.examples.randomizedrunner;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.JUnit4MethodProvider;
import com.carrotsearch.randomizedtesting.MixWithSuiteName;
import com.carrotsearch.randomizedtesting.annotations.SeedDecorators;
import com.carrotsearch.randomizedtesting.annotations.TestMethodProviders;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction.Action;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakAction;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakGroup.Group;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakGroup;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakZombies.Consequence;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakZombies;
import com.carrotsearch.randomizedtesting.annotations.TimeoutSuite;

@RunWith(DelegatingRunner.class)
@TestMethodProviders({
  JUnit4MethodProvider.class
})
@SeedDecorators({MixWithSuiteName.class}) // See LUCENE-3995 for rationale.
@ThreadLeakScope(Scope.SUITE)
@ThreadLeakGroup(Group.MAIN)
@ThreadLeakAction({Action.WARN, Action.INTERRUPT})
@ThreadLeakLingering(linger = 20000) // Wait long for leaked threads to complete before failure. zk needs this.
@ThreadLeakZombies(Consequence.IGNORE_REMAINING_TESTS)
@TimeoutSuite(millis = 200000000)
public class TestExample extends Assert {

  @BeforeClass
  private static void beforeClassPrivate() {
    System.out.println("beforeClassPrivate");
  }

  @Test
  public void test() {
    System.out.println("test");
  }
  
  @Test
  public void noPermissions() {
    System.err.println("checking permission system.");
    try {
      System.setProperty("foo", "bar");
      fail();
    } catch (SecurityException e) {
      // Expected!
    }
  }
}
