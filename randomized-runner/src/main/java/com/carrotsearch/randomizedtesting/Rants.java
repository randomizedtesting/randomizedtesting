package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Rants about blocker limitations of JUnit...
 */
final class Rants {
  enum RantType {
    // General
    ANNOYANCE,
    DAMN_TERRIBLE,
    WTF,

    // Personal
    ISHOULDHAVEBECOMEALAWYER
  }
  
  /**
   * This if freaking dumb... there's absolutely no way to carry test class/ test name
   * separately from the display name, so we can't easily include seed info on the test
   * case. If we do, Eclipse complains it cannot find the target class/ test name. If we don't,
   * Eclipse's JUnit runner gets confused and doesn't show test case execution properly.
   * 
   * We can't even use a proxy or a subclass because Description has a private constructor. Eh.
   * 
   * Having a Description properly indicate the test case/ class is useful because we could re-run
   * a concrete repetition of a given test from the UI. Currently this is impossible - we can
   * re-run the entire iteration sequence only (or fix the seed on the method, but this requires
   * changes to the code). 
   */
  public static RantType RANT_1 = RantType.DAMN_TERRIBLE;
  
  /**
   * Default assumption methods (and constructors in AssumptionViolatedException)
   * do not allow specifying a custom message? 
   */
  public static RantType RANT_2 = RantType.ANNOYANCE;
  
  /**
   * Why is failed assumption propagated as a Failure? This is weird an unnatural.
   */
  public static RantType RANT_3 = RantType.DAMN_TERRIBLE;

  /**
   * JUnit is inconsistent in how it treats annotations on methods. Some of them are "inherited" and
   * some require presence on the exact same {@link Method} as the one used for testing. This has awkward
   * side effects, for example {@link Ignore} and {@link Test} must co-exist on the same method, not
   * on virtual method hierarchy. You cannot make {@link Test} methods protected and publish them in 
   * subclasses. Shadowing of {@link BeforeClass} methods is inconsistent (non-annotated shadowed method
   * will not be called, shadowed method annotated with {@link BeforeClass} prevents the shadowed method 
   * from being called), etc.
   */
  public static RantType RANT_4 = RantType.DAMN_TERRIBLE;
}
