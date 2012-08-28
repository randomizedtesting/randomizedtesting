package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TruncatedLog {
  private static Deque<Runnable> tasks = new ArrayDeque<Runnable>();
  
  @BeforeClass
  public static void setupTasks() {
    tasks.clear();

    tasks.addLast(new Runnable() {
      @Override
      public void run() {
        byte [] value = new byte [1024 * 1024];
        for (int i = 0; i < value.length; i++) {
          value[i] = (byte) i;
        }

        try {
          System.out.write(value);
          System.out.flush();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        Assert.assertFalse("foobar", true);
      }
    });

    tasks.addLast(new Runnable() {
      @Override
      public void run() {
        System.exit(66);
      }
    });
  }

  @Test
  public void test1() {
    tasks.removeFirst().run();
  }

  @Test
  public void test2() {
    tasks.removeFirst().run();
  }
}
