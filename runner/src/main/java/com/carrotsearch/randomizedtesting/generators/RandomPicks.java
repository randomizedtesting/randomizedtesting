package com.carrotsearch.randomizedtesting.generators;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Random selections of objects.
 */
public final class RandomPicks {
  /**
   * Pick a random object from the given array.
   */
  public static <T> T randomFrom(Random r, T [] array) {
    return array[r.nextInt(array.length)];
  }

  /**
   * Pick a random object from the given list.
   */
  public static <T> T randomFrom(Random r, List<T> list) {
    return list.get(r.nextInt(list.size()));
  }

  /**
   * Pick a random object from the collection. Requires linear scanning.
   */
  public static <T> T randomFrom(Random r, Collection<T> collection) {
    int pick = r.nextInt(collection.size());
    T value = null;
    for (Iterator<T> i = collection.iterator();; pick--) {
      value = i.next();
      if (pick == 0)
        break;
    }
    return value;
  }
}
