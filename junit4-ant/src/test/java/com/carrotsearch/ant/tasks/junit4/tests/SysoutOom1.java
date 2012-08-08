package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class SysoutOom1 {
  @Test
  public void writealot() {
    char [] CHARS = "ABCDEFGHIJKLOMNOPQRSTUVWXY1208941239846926Y932".toCharArray();
    char [] chars = new char [200];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = CHARS[i % CHARS.length];
    }

    int emitChars = 1024 * 1024 * 25;
    while (emitChars > 0) {
      System.out.print(chars);
      emitChars -= chars.length;
    }
  }
}
