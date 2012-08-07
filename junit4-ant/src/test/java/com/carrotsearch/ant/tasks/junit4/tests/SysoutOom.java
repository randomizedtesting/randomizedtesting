package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class SysoutOom {
  @Test
  public void writealot() {
    char [] CHARS = "ABCDEFGHIJKLOMNOPQRSTUVWXY1208941239846926Y932".toCharArray();
    char [] chars = new char [1024];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = CHARS[i % CHARS.length];
    }

    for (int i = 0; i < 1024 * 25; i++) {
      System.out.println(chars);
    }
  }
}
