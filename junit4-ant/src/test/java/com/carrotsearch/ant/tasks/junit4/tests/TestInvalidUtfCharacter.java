package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class TestInvalidUtfCharacter {
  @Test
  public void emitInvalidCharacter() throws Exception {
    String msg = "Invalid char: >\u0002< >\u0000<";
    System.out.println(msg);
    System.out.flush();
    throw new Exception(msg);
  }
}
