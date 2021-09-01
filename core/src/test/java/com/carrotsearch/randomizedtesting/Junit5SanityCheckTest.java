package com.carrotsearch.randomizedtesting;

import org.junit.jupiter.api.Test;

public class Junit5SanityCheckTest {
  @Test
  public void testPassing() {
    // Ok.
    System.out.println("Passing.");
  }

  @Test
  public void testFailingOnAssertion() {
    System.out.println("failingOnAssertion");
    assert false : "Failing.";
  }

  @Test
  public void testFailingWithException() {
    System.out.println("failingWithException");
    throw new RuntimeException("Failing with exception.");
  }
}
