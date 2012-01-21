package com.carrotsearch.ant.tasks.junit4.events.aggregated;

public enum TestStatus {
  OK,
  
  IGNORED,
  IGNORED_ASSUMPTION,

  FAILURE,
  ERROR,
}
