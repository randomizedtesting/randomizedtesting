package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * A string generator that emits valid unicodeGenerator codepoints.
 */
public class UnicodeGenerator extends StringGenerator {
  private final static int SURROGATE_RANGE = Character.MAX_SURROGATE - Character.MIN_SURROGATE + 1;
  private final static int CODEPOINT_RANGE = Character.MAX_CODE_POINT - SURROGATE_RANGE;

  @Override
  public String ofCodeUnitsLength(Random r, int minCodeUnits, int maxCodeUnits) {
    int length = RandomNumbers.randomIntBetween(r, minCodeUnits, maxCodeUnits);
    char [] chars = new char [length];
    for (int i = 0; i < chars.length;) {
      final int t = RandomNumbers.randomIntBetween(r, 0, 4);
      if (t == 0 && i < length - 1) {
        // Make a surrogate pair
        chars[i++] = (char) RandomNumbers.randomIntBetween(r, 0xd800, 0xdbff); // high
        chars[i++] = (char) RandomNumbers.randomIntBetween(r, 0xdc00, 0xdfff); // low
      } else if (t <= 1) {
        chars[i++] = (char) RandomNumbers.randomIntBetween(r,      0, 0x007f);
      } else if (t == 2) {
        chars[i++] = (char) RandomNumbers.randomIntBetween(r,   0x80, 0x07ff);
      } else if (t == 3) {
        chars[i++] = (char) RandomNumbers.randomIntBetween(r,  0x800, 0xd7ff);
      } else if (t == 4) {
        chars[i++] = (char) RandomNumbers.randomIntBetween(r, 0xe000, 0xffff);
      }
    }
    return new String(chars);
  }

  @Override
  public String ofCodePointsLength(Random r, int minCodePoints, int maxCodePoints) {
    int length = RandomNumbers.randomIntBetween(r, minCodePoints, maxCodePoints);
    int [] chars = new int [length];
    for (int i = 0; i < chars.length; i++) {
      int v = RandomNumbers.randomIntBetween(r, 0, CODEPOINT_RANGE);
      if (v >= Character.MIN_SURROGATE)
        v += SURROGATE_RANGE;
      chars[i] = v;
    }
    return new String(chars, 0, chars.length);
  }

  /** 
   * Returns a random string that will have a random UTF-8 representation length between
   * <code>minUtf8Length</code> and <code>maxUtf8Length</code>.
   * 
   * @param minUtf8Length Minimum UTF-8 representation length (inclusive).
   * @param maxUtf8Length Maximum UTF-8 representation length (inclusive).
   */
  public String ofUtf8Length(Random r, int minUtf8Length, int maxUtf8Length) {
    final int length = RandomNumbers.randomIntBetween(r, minUtf8Length, maxUtf8Length);
    final char[] buffer = new char [length * 3];
    int bytes = length;
    int i = 0;
    for (; i < buffer.length && bytes != 0; i++) {
      int t;
      if (bytes >= 4) {
        t = r.nextInt(5);
      } else if (bytes >= 3) {
        t = r.nextInt(4);
      } else if (bytes >= 2) {
        t = r.nextInt(2);
      } else {
        t = 0;
      }
      if (t == 0) {
        buffer[i] = (char) RandomNumbers.randomIntBetween(r, 0, 0x7f);
        bytes--;
      } else if (1 == t) {
        buffer[i] = (char) RandomNumbers.randomIntBetween(r, 0x80, 0x7ff);
        bytes -= 2;
      } else if (2 == t) {
        buffer[i] = (char) RandomNumbers.randomIntBetween(r, 0x800, 0xd7ff);
        bytes -= 3;
      } else if (3 == t) {
        buffer[i] = (char) RandomNumbers.randomIntBetween(r, 0xe000, 0xffff);
        bytes -= 3;
      } else if (4 == t) {
        // Make a surrogate pair
        buffer[i++] = (char) RandomNumbers.randomIntBetween(r, 0xd800, 0xdbff); // high
        buffer[i] = (char) RandomNumbers.randomIntBetween(r, 0xdc00, 0xdfff);   // low
        bytes -= 4;
      }
    }
    return new String(buffer, 0, i);
  }
}
