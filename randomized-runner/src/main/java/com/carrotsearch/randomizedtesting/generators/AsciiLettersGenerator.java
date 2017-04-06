package com.carrotsearch.randomizedtesting.generators;

/**
 * A generator emitting simple ASCII characters from the set
 * (newlines not counted):
 * <pre>
 * abcdefghijklmnopqrstuvwxyz
 * ABCDEFGHIJKLMNOPQRSTUVWXYZ
 * </pre>
 */
public class AsciiLettersGenerator extends CodepointSetGenerator {
  private final static char [] CHARS = 
      ("abcdefghijklmnopqrstuvwxyz" + 
       "ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();

  public AsciiLettersGenerator() {
    super(CHARS);
  }
}
