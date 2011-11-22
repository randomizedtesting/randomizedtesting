package com.carrotsearch.randomizedtesting.examples;

import java.util.Arrays;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;

public class TestParameters extends RandomizedTest {
  private String name;
  private int age;

  public TestParameters(@Name("age") int age, @Name("name") String name) {
    this.name = name;
    this.age = age;
  }

  @Test
  public void testPerson() {
    assertTrue("Won't like: " + name, 
        age >= 18 && age <= 21);
  }

  @ParametersFactory
  public static Iterable<Object[]> parameters() {
    return Arrays.asList($$(
        $(18, "Dolly"), 
        $(25, "Barbie")));
  }
}

