package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Assert;
import org.junit.Test;

public class TestEscaping {
  @Test
  public void checkEscapes() {
    // success it is.
    System.out.println(System.getProperty("sysprop.key"));
    System.out.println(System.getProperty("sysprop.key2"));
    System.out.println(System.getProperty("sysprop.key3"));
    System.out.println(System.getenv("env.variable"));

    Assert.assertEquals("${nonexistent-1}", System.getProperty("sysprop.key"));
    Assert.assertEquals("abc def", System.getProperty("sysprop.key2"));
    Assert.assertEquals("%PATH%", System.getProperty("sysprop.key3"));
    Assert.assertEquals("${nonexistent-3}", System.getenv("env.variable"));
  }
  
  public static void main(String[] args) {
    new TestEscaping().checkEscapes();
  }
}
