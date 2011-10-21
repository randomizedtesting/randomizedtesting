package com.carrotsearch.randomizedtesting.examples;

import org.junit.*;


@SuppressWarnings("unused")
public class TestClass2 extends TestClass1 {
  @BeforeClass private static void beforeClass() { System.out.println("class 2: beforeClass"); } 
  @Before      private        void before()      { System.out.println("  class 2: before"); }
  @Test        public         void test2_1()     { System.out.println("    class 2: test1"); }
  @Test        public         void test2_2()     { System.out.println("    class 2: test2"); }
  @Test        public         void test2_3()     { System.out.println("    class 2: test3"); }
  @After       private        void after()       { System.out.println("  class 2: after"); }
  @AfterClass  private static void afterClass()  { System.out.println("class 2: afterClass"); }
}