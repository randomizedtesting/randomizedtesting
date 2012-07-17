package com.carrotsearch.randomizedtesting;

final class Classes {
  private Classes() {}

  public static String simpleName(Class<?> c) {
    String fullName = c.getName();
    int lastDot = fullName.lastIndexOf(".");
    if (lastDot < 0) {
      return fullName;
    } else {
      return fullName.substring(lastDot + 1);
    }
  }
}
