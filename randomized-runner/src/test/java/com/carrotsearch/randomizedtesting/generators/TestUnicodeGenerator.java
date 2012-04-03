package com.carrotsearch.randomizedtesting.generators;

public class TestUnicodeGenerator extends StringGeneratorTestBase {
  public TestUnicodeGenerator() {
    super(new UnicodeGenerator());
  }
}
