package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class SysoutOom1 {
  @Test
  public void writealot() {
    write("012345678901234567890123456789".toCharArray());
  }

  protected final void write(char[] charArray) {
    char [] chars = new char [200];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = charArray[i % charArray.length];
    }

    int emitChars = 1024 * 1024 * 25;
    while (emitChars > 0) {
      System.out.print(chars);
      emitChars -= chars.length;
    }
  }
}
