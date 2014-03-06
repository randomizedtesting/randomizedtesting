package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

@SuppressWarnings("all")
public class NotInstantiable {
  static {
    System.out.println("Can't instantiate me." + 1 / ((Long) null));
  }

  public NotInstantiable() {}

  @Test
  public void method() {}
}
