package com.carrotsearch.randomizedtesting.generators;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

import static org.junit.Assert.*;

/**
 * Base class for testing {@link StringGenerator}s.
 */
public abstract class StringGeneratorTestBase extends RandomizedTest {
  protected final StringGenerator generator;

  protected StringGeneratorTestBase(StringGenerator generator) {
    this.generator = generator;
  }

  @Test @Repeat(iterations = 10)
  public void checkFixedCodePointLength() {
    int codepoints = iterationFix(randomIntBetween(1, 100));
    String s = generator.ofCodePointsLength(getRandom(), codepoints, codepoints);
    assertEquals(s, codepoints, s.codePointCount(0, s.length()));
  }

  @Test @Repeat(iterations = 10)
  public void checkRandomCodePointLength() {
    int from = iterationFix(randomIntBetween(1, 100));
    int to = from + randomIntBetween(0, 100);

    String s = generator.ofCodePointsLength(getRandom(), from, to);
    int codepoints = s.codePointCount(0, s.length());
    assertTrue(codepoints + " not within " + 
        from + "-" + to, from <= codepoints && codepoints <= to);
  }

  @Test @Repeat(iterations = 10)
  public void checkFixedCodeUnitLength() {
    int codeunits = iterationFix(randomIntBetween(1, 100));
    String s = generator.ofCodeUnitsLength(getRandom(), codeunits, codeunits);
    assertEquals(s, codeunits, s.length());
    assertEquals(s, codeunits, s.toCharArray().length);
  }

  @Test @Repeat(iterations = 10)
  public void checkRandomCodeUnitLength() {
    int from = iterationFix(randomIntBetween(1, 100));
    int to = from + randomIntBetween(0, 100);

    String s = generator.ofCodeUnitsLength(getRandom(), from, to);
    int codeunits = s.length();
    assertTrue(codeunits + " not within " + 
        from + "-" + to, from <= codeunits && codeunits <= to);
  }

  @Test
  public void checkZeroLength() {
    assertEquals("", generator.ofCodePointsLength(getRandom(), 0, 0));
    assertEquals("", generator.ofCodeUnitsLength(getRandom(), 0, 0));
  }
  
  /**
   * Correct the count if a given generator doesn't support all possible values (in tests).
   */
  protected int iterationFix(int i) {
    return i;
  }
}
