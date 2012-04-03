package com.carrotsearch.randomizedtesting.generators;

public class TestRealisticUnicodeGenerator extends StringGeneratorTestBase {
  public TestRealisticUnicodeGenerator() {
    super(new UnicodeGenerator());
  }
}
