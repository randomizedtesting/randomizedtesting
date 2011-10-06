package com.carrotsearch.randomizedtesting.examples;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.Repeat;

/**
 * Just an eyeballing at the output and fiddling with stuff.
 */
// @Seed("deadbeef")
// @Repeat(100)
@RunWith(RandomizedRunner.class)
public class TestEyeBalling {
  @BeforeClass
  public static void setup() {
    info("before class");
  }

  @Before
  public void testSetup() {
    info("before test");
  }

  @Repeat(iterations = 4)
  @Test
  public void test1() {
    info("test1");
  }

  @Test
  public void test2() {
    info("test2");
  }

  @After
  public void testCleanup() {
    info("after test");
  }

  @AfterClass
  public static void cleanup() {
    info("after class");
  }

  private static void info(String msg) {
    System.out.println(msg + " " + 
        RandomizedContext.current().getRandomness());
  }
}

