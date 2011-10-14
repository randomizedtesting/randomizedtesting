package com.carrotsearch.randomizedtesting.examples.barcelona;

import org.junit.*;

import com.carrotsearch.randomizedtesting.RandomizedTest;

@SuppressWarnings("unused")
public class TestClass1 extends RandomizedTest {
  @BeforeClass private static void beforeClass() { System.out.println("class 1: beforeClass"); } 
  @Before      private        void before()      { System.out.println("  class 1: before"); }
  @Test        public         void test1_1()     { System.out.println("    class 1: test1"); }
  @Test        public         void test1_2()     { System.out.println("    class 1: test2"); }
  @Test        public         void test1_3()     { System.out.println("    class 1: test3"); }
  @After       private        void after()       { System.out.println("  class 1: after"); }
  @AfterClass  private static void afterClass()  { System.out.println("class 1: afterClass"); }
}