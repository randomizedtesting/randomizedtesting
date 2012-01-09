package com.carrotsearch.examples.randomizedrunner;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.*;
import com.carrotsearch.randomizedtesting.annotations.Validators;
import com.carrotsearch.randomizedtesting.validators.*;

/**
 * {@link RandomizedRunner} respects an on-suite class {@link Validators}
 * annotation and instantiates classes that implement {@link ClassValidator}.
 * This allows custom validation of test suites subclassed from a single parent
 * hierarchy for example.
 * 
 * <p>
 * This is a somewhat experimental feature. We wrote several validators that we
 * believe are quite handly. For example {@link NoJUnit3TestMethods} validator
 * will ensure there are no accidental JUnit3-style test methods without proper
 * {@link Test} annotation. {@link NoTestMethodOverrides} will ensure no hook
 * methods are overriden (assuming overriding is accidental and shouldn't
 * happen). Similarly, {@link NoHookMethodShadowing} will ensure no static hook
 * methods are shadowed (shadowed hooks are not executed by JUnit and this
 * behavior is simulated by {@link RandomizedRunner}).
 */
@Validators({
  NoJUnit3TestMethods.class
})
public class Test013Validators extends RandomizedTest {
  public void testOopsImNot() {}
  
  @Test
  public void testIAm() {}
}
