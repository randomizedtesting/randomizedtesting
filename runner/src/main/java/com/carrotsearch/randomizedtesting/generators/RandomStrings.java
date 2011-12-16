package com.carrotsearch.randomizedtesting.generators;

import java.util.Random;

/**
 * A facade to various implementations of {@link StringGenerator} interface.
 */
public final class RandomStrings {
  public final static RealisticUnicodeGenerator realisticUnicodeGenerator = new RealisticUnicodeGenerator();
  public final static UnicodeGenerator unicodeGenerator = new UnicodeGenerator();
  public final static ASCIIGenerator asciiGenerator = new ASCIIGenerator();

  // Ultra wide monitor required to read the source code :)

  public static String randomAsciiOfLengthBetween                     (Random r, int minCodeUnits, int maxCodeUnits)   {return asciiGenerator.ofCodeUnitsLength(r, minCodeUnits, maxCodeUnits); }
  public static String randomAsciiOfLength                            (Random r, int codeUnits)                        {return asciiGenerator.ofCodeUnitsLength(r, codeUnits, codeUnits); }

  public static String randomUnicodeOfLengthBetween                   (Random r, int minCodeUnits, int maxCodeUnits)   {return unicodeGenerator.ofCodeUnitsLength(r, minCodeUnits, maxCodeUnits); }
  public static String randomUnicodeOfLength                          (Random r, int codeUnits)                        {return unicodeGenerator.ofCodeUnitsLength(r, codeUnits, codeUnits); }
  public static String randomUnicodeOfCodepointLengthBetween          (Random r, int minCodePoints, int maxCodePoints) {return unicodeGenerator.ofCodePointsLength(r, minCodePoints, maxCodePoints); }
  public static String randomUnicodeOfCodepointLength                 (Random r, int codePoints)                       {return unicodeGenerator.ofCodePointsLength(r, codePoints, codePoints); }

  public static String randomRealisticUnicodeOfLengthBetween          (Random r, int minCodeUnits, int maxCodeUnits)   {return realisticUnicodeGenerator.ofCodeUnitsLength(r, minCodeUnits, maxCodeUnits); }
  public static String randomRealisticUnicodeOfLength                 (Random r, int codeUnits)                        {return realisticUnicodeGenerator.ofCodeUnitsLength(r, codeUnits, codeUnits); }  
  public static String randomRealisticUnicodeOfCodepointLengthBetween (Random r, int minCodePoints, int maxCodePoints) {return realisticUnicodeGenerator.ofCodePointsLength(r, minCodePoints, maxCodePoints); }
  public static String randomRealisticUnicodeOfCodepointLength        (Random r, int codePoints)                       {return realisticUnicodeGenerator.ofCodePointsLength(r, codePoints, codePoints); }
}
