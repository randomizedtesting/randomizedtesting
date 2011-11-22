package com.carrotsearch.randomizedtesting.examples;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

@RunWith(RandomizedRunner.class)
public class TestSomething {
  @Test 
  public void testA() {
    new Thread() {
      public void run() {
        while (true);
      }
    }.start();
  }

  @Test 
  public void testB() {}
}