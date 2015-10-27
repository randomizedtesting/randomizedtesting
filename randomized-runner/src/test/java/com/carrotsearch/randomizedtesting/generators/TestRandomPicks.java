package com.carrotsearch.randomizedtesting.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

import static org.junit.Assert.*;

public class TestRandomPicks extends RandomizedTest {
  @Test(expected = IllegalArgumentException.class)
  public void testRandomFromEmptyCollection() {
    RandomPicks.randomFrom(getRandom(), new HashSet<Object>());
  }

  @Test
  public void testRandomFromCollection() {
    Object t = new Object();
    Object r = RandomPicks.randomFrom(getRandom(), new HashSet<Object>(Arrays.asList(t)));
    assertSame(r, t);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRandomFromList() {
    RandomPicks.randomFrom(getRandom(), new ArrayList<Object>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRandomFromArray() {
    RandomPicks.randomFrom(getRandom(), new Object[] {});
  }
}
