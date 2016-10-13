package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * A string generator from a predefined set of codepoints or characters.
 */
public class CodepointSetGenerator extends StringGenerator {
  final int [] bmp;
  final int [] supplementary;
  final int [] all;

  /**
   * All characters must be from BMP (no parts of surrogate pairs allowed).
   */
  public CodepointSetGenerator(char[] chars) {
    this.bmp = new int [chars.length];
    this.supplementary = new int [0];

    for (int i = 0; i < chars.length; i++) {
      bmp[i] = ((int) chars[i]) & 0xffff;

      if (isSurrogate(chars[i])) {
        throw new IllegalArgumentException("Value is part of a surrogate pair: 0x" 
            + Integer.toHexString(bmp[i]));
      }
    }

    this.all = concat(bmp, supplementary);
    if (all.length == 0) {
      throw new IllegalArgumentException("Empty set of characters?");
    }
  }

  /**
   * Parse the given {@link String} and split into BMP and supplementary codepoints.
   */
  public CodepointSetGenerator(String s) {
    int bmps = 0;
    int supplementaries = 0;
    for (int i = 0; i < s.length();) {
      int codepoint = s.codePointAt(i);
      if (Character.isSupplementaryCodePoint(codepoint)) {
        supplementaries++;
      } else {
        bmps++;
      }
      
      i += Character.charCount(codepoint);
    }

    this.bmp = new int [bmps];
    this.supplementary = new int [supplementaries];
    for (int i = 0; i < s.length();) {
      int codepoint = s.codePointAt(i);
      if (Character.isSupplementaryCodePoint(codepoint)) {
        supplementary[--supplementaries] = codepoint;
      } else {
        bmp[--bmps] = codepoint;
      }
      
      i += Character.charCount(codepoint);
    }

    this.all = concat(bmp, supplementary);
    if (all.length == 0) {
      throw new IllegalArgumentException("Empty set of characters?");
    }
  }

  @Override
  public String ofCodeUnitsLength(Random r, int minCodeUnits, int maxCodeUnits) {
    int length = RandomNumbers.randomIntBetween(r, minCodeUnits, maxCodeUnits);

    // Check and cater for odd number of code units if no bmp characters are given.
    if (bmp.length == 0 && isOdd(length)) { 
      if (minCodeUnits == maxCodeUnits) {
        throw new IllegalArgumentException("Cannot return an odd number of code units "
            + " when surrogate pairs are the only available codepoints.");
      } else {
        // length is odd so we move forward or backward to the closest even number.
        if (length == minCodeUnits) {
          length++;
        } else {
          length--;
        }
      }
    }

    int [] codepoints = new int [length];
    int actual = 0;
    while (length > 0) {
      if (length == 1) {
        codepoints[actual] = bmp[r.nextInt(bmp.length)];
      } else {
        codepoints[actual] = all[r.nextInt(all.length)];
      }

      if (Character.isSupplementaryCodePoint(codepoints[actual])) {
        length -= 2;
      } else {
        length -= 1;
      }
      actual++;
    }
    return new String(codepoints, 0, actual);
  }

  @Override
  public String ofCodePointsLength(Random r, int minCodePoints, int maxCodePoints) {
    int length = RandomNumbers.randomIntBetween(r, minCodePoints, maxCodePoints);
    int [] codepoints = new int [length];
    while (length > 0) {
      codepoints[--length] = all[r.nextInt(all.length)];
    }
    return new String(codepoints, 0, codepoints.length);
  }

  /** Is a given number odd? */
  private boolean isOdd(int v) {
    return (v & 1) != 0;
  }

  private int[] concat(int[]... arrays) {
    int totalLength = 0;
    for (int[] a : arrays) totalLength += a.length;
    int [] concat = new int [totalLength];
    for (int i = 0, j = 0; j < arrays.length;) {
      System.arraycopy(arrays[j], 0, concat, i, arrays[j].length);
      i += arrays[j].length;
      j++;
    }
    return concat;
  }

  private boolean isSurrogate(char chr) {
    return (chr >= 0xd800 && chr <= 0xdfff);
  }
}
