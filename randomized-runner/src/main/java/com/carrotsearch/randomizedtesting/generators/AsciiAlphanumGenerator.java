package com.carrotsearch.randomizedtesting.generators;

/**
 * A generator emitting simple ASCII alphanumeric letters and numbers 
 * from the set (newlines not counted):
 * <pre>
 * abcdefghijklmnopqrstuvwxyz
 * ABCDEFGHIJKLMNOPQRSTUVWXYZ
 * 0123456789
 * </pre>
 */
public class AsciiAlphanumGenerator extends CodepointSetGenerator {
  private final static char [] CHARS = 
      ("abcdefghijklmnopqrstuvwxyz" + 
       "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
       "0123456789").toCharArray();

  public AsciiAlphanumGenerator() {
    super(CHARS);
  }
}
