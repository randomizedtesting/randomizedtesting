package com.carrotsearch.randomizedtesting.generators;

/**
 * A generator emitting simple ASCII characters from the set
 * (newlines not counted):
 * <pre>
 * abcdefghijklmnopqrstuvwxyz
 * ABCDEFGHIJKLMNOPQRSTUVWXYZ
 * </pre>
 */
public class ASCIIGenerator extends CodepointSetGenerator {
  private final static char [] ASCII_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  public ASCIIGenerator() {
    super(ASCII_SET);
  }
}
