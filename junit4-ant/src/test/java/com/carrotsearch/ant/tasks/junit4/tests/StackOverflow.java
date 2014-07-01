package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class StackOverflow {
  @Test
  public void stackoverflow() {
    for (int i = 0; i < 100; i++) {
      try {
        doStackOverflowAndPrint(0);
      } catch (StackOverflowError e) {
        // Should have failed inside the runner's infrastructure?
      }
    }
    System.exit(0);
  }
  
  private void doStackOverflowAndPrint(int i) {
    System.out.println("Stack overflow attempt: " + i);
    doStackOverflowAndPrint(i + 1);
  }
}
