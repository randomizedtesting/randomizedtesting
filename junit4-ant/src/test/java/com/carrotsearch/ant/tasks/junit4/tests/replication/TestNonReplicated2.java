package com.carrotsearch.ant.tasks.junit4.tests.replication;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

public class TestNonReplicated2 extends RandomizedTest {
  @Test
  public void nonReplicatedTest() {
    System.out.println("Non-replicated test, VM: " + System.getProperty("junit4.childvm.id"));
  }
}
