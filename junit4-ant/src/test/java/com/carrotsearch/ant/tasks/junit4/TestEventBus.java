package com.carrotsearch.ant.tasks.junit4;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class TestEventBus extends RandomizedTest {
  static volatile Thread current;
  
  @Test
  @ThreadLeakLingering(linger = 1000)
  public void testEventBusConcurrency() throws Exception {
    final EventBus bus = new EventBus();
    
    bus.register(new Object() {
      @Subscribe
      @SuppressWarnings("unused")
      public void onEvent(Integer nop) throws Exception {
        current = Thread.currentThread();
        Thread.sleep(20);
        if (current != Thread.currentThread()) {
          throw new RuntimeException();
        }
      }
    });
    
    final List<Callable<Object>> tasks = Lists.newArrayList();
    for (int i = 0; i < 50; i++) {
      tasks.add(new Callable<Object>() {
        public Object call() throws Exception {
          bus.post(1);
          return null;
        }
      });
    }
    final ExecutorService executor = Executors.newFixedThreadPool(5);
    for (Future<Object> f : executor.invokeAll(tasks)) {
      f.get();
    }
    executor.shutdown();
  }
}
