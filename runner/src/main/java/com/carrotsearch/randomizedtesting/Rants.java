package com.carrotsearch.randomizedtesting;

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
   * TODO: this if freaking dumb... there's absolutely no way to carry test class/ test name
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
   * TODO: this if weird default assumption methods (and constructors in AssumptionViolatedException)
   * do not allow specifying a custom message? 
   */
  public static RantType RANT_2 = RantType.ANNOYANCE;  
}
