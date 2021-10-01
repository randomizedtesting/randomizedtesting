package com.carrotsearch.randomizedtesting.generators;

public class TestBiasedUnicodeGenerator extends StringGeneratorTestBase {
  public TestBiasedUnicodeGenerator() {
    super(new BiasedUnicodeGenerator());
  }
}
