package com.carrotsearch.ant.tasks.junit4;

public final class Pluralize {
  private Pluralize() {}

  public static String pluralize(int count, String word) {
    if (count != 1) {
      word += "s";
    }
    return word;
  }
}
