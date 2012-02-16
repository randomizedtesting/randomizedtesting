package com.carrotsearch.ant.tasks.junit4.balancers;

import java.util.Comparator;

/**
 * A suite with the cost hint.
 */
public final class SuiteHint {
  public static final Comparator<SuiteHint> DESCENDING_BY_WEIGHT = new Comparator<SuiteHint>() {
    @Override
    public int compare(SuiteHint o1, SuiteHint o2) {
      if (o1.cost == o2.cost)
        return o1.suiteName.compareTo(o2.suiteName);

      if (o1.cost < o2.cost)
        return 1;
      else
        return -1;
    }
  };

  public final String suiteName;
  public final long cost;

  public SuiteHint(String suiteName, long weight) {
    this.suiteName = suiteName;
    this.cost = weight;
  }
}