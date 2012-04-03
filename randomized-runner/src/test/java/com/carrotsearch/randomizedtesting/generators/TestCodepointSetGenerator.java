package com.carrotsearch.randomizedtesting.generators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  TestCodepointSetGenerator.CodepointSetOnChars.class,
  TestCodepointSetGenerator.CodepointSetOnCodePoints.class,
  TestCodepointSetGenerator.CodepointSetOnSurrogatesOnly.class
})
public class TestCodepointSetGenerator {
  private final static int [] codepoints = {
    'a', 'b', 'c', 'd',
    0xd7ff,
    0xffff,
    0x10000,
    0x1D11E,
    0x10FFFD,
  };

  private final static int [] surrogates = {
    0x10000,
    0x1D11E,
    0x10FFFD,
  };

  private final static String withSurrogates = new String(codepoints, 0, codepoints.length);

  public static class CodepointSetOnChars extends StringGeneratorTestBase {
    public CodepointSetOnChars() {
      super(new CodepointSetGenerator(new char[] {
          'a', 'b', 'c', 'd',
          0x100,
          0xd7ff,
          0xffff
      }));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSurrogatesInConstructor() {
      new CodepointSetGenerator(withSurrogates.toCharArray());
    }
  }

  public static class CodepointSetOnCodePoints extends StringGeneratorTestBase {
    public CodepointSetOnCodePoints() {
      super(new CodepointSetGenerator(withSurrogates));      
    }
  }
  
  public static class CodepointSetOnSurrogatesOnly extends StringGeneratorTestBase {
    public CodepointSetOnSurrogatesOnly() {
      super(new CodepointSetGenerator(new String(surrogates, 0, surrogates.length)));      
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOddCodePoints() {
      generator.ofCodeUnitsLength(getRandom(), 3, 3);
    }

    @Override
    protected int iterationFix(int i) {
      return i & ~1;      // Even only.
    }
  }  
}
