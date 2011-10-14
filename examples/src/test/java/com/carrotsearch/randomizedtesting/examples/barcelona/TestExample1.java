package com.carrotsearch.randomizedtesting.examples.barcelona;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Seed;

public class TestExample1 extends RandomizedTest {
  
  @Test
  public void testBarcelona() {
    runMethod(new Random());
  }

  @Test
  @Seed("487a51b") // get unlucky...
  public void testBarcelona2() {
    runMethod(
        RandomizedContext.current().getRandom());
  }

  public void runMethod(Random r) {
    String [] words = {
        "lucene", "barcelona", 
        "oracle", // "sun", 
        "beach", "fun"};
    for (int i = 0; i < 100; i++) {
      System.out.println(
          words[Math.abs(r.nextInt()) % words.length]);
    }    
  }
}
