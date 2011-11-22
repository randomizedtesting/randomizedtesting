package com.carrotsearch.randomizedtesting.examples;

import java.util.Random;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Seed;

public class TestExample1 extends RandomizedTest {
  @Test
  public void testPoznanWords() {
    poznanWords(new Random());
  }

  @Test
  @Seed("487a51b") // get unlucky...
  public void testPoznanWords2() {
    poznanWords(RandomizedContext.current().getRandom());
  }

  String[] words = {"Poznan", "autumn", /* "sun", */"oracle"};
  public void poznanWords(Random r) {
    for (int i = 0; i < 100; i++) {
      int index = Math.abs(r.nextInt()) % words.length;
      System.out.println(words[index]);
    }
  }
}
