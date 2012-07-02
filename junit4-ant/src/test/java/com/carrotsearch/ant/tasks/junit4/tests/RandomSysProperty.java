package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class RandomSysProperty {
  @Test
  public void test1() {
    for (String s : new String [] {
        "prefix.dummy1",
        "prefix.dummy2",
        "replaced.dummy1",
        "replaced.dummy2",
    }) {
      System.out.println(s + "=" + System.getProperty(s));
    }
  }
}
