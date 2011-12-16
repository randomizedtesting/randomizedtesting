package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * A {@link StringGenerator} generates random strings composed of characters. What these characters
 * are and their distribution depends on a subclass.
 * 
 * @see String
 */
public abstract class StringGenerator {
  /**
   * An alias for {@link #ofCodeUnitsLength(Random, int, int)}.
   */
  public String ofStringLength(Random r, int minCodeUnits, int maxCodeUnits) {
    return ofCodeUnitsLength(r, minCodeUnits, maxCodeUnits);
  }

  /**
   * @return Returns a string of variable length between <code>minCodeUnits</code> (inclusive)
   * and <code>maxCodeUnits</code> (inclusive) length. Code units are essentially
   * an equivalent of <code>char</code> type, see {@link String} class for
   * explanation.  
   * 
   * @param minCodeUnits Minimum number of code units (inclusive).
   * @param maxCodeUnits Maximum number of code units (inclusive).
   * @throws IllegalArgumentException Thrown if the generator cannot emit random string
   * of the given unit length. For example a generator emitting only extended unicodeGenerator
   * plane characters (encoded as surrogate pairs) will not be able to emit an odd number
   * of code units.
   */
  public abstract String ofCodeUnitsLength(Random r, int minCodeUnits, int maxCodeUnits);

  /**
   * @return Returns a string of variable length between <code>minCodePoints</code> (inclusive)
   * and <code>maxCodePoints</code> (inclusive) length. Code points are full unicodeGenerator
   * codepoints or an equivalent of <code>int</code> type, see {@link String} class for
   * explanation. The returned {@link String#length()} may exceed <code>maxCodeUnits</code>
   * because certain code points may be encoded as surrogate pairs.
   * 
   * @param minCodePoints Minimum number of code points (inclusive).
   * @param maxCodePoints Maximum number of code points (inclusive).
   */
  public abstract String ofCodePointsLength(Random r, int minCodePoints, int maxCodePoints);
}
