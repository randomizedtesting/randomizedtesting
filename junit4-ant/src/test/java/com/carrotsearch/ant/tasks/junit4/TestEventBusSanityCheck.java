package com.carrotsearch.ant.tasks.junit4;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import static org.junit.Assert.*;

@ThreadLeakScope(Scope.SUITE)
@ThreadLeakLingering(linger = 1000)
public class TestEventBusSanityCheck extends RandomizedTest {
  static class SlaveIdle {
    public void finished() {
      sleep(randomIntBetween(1, 50));
    }
    
    public void newSuite(String suiteName) {
      sleep(randomIntBetween(1, 2));
    }
  }
  
  @Test
  @Repeat(iterations = 100)
  public void testArrayQueueReentrance() throws Exception {
    // Mockups.
    final List<String> foo = new ArrayList<>();
    for (int i = randomIntBetween(2, 1000); --i > 0;) {
      foo.add(randomAsciiLettersOfLength(20));
    }
    final EventBus aggregatedBus = new EventBus("aggregated");

    final AtomicBoolean hadErrors = new AtomicBoolean();
    
    // Code mirrors JUnit4's behavior.
    final Deque<String> stealingQueue = new ArrayDeque<String>(foo);
    aggregatedBus.register(new Object() {
      volatile Thread foo;

      @Subscribe
      public void onSlaveIdle(SlaveIdle slave) {
        final Thread other = foo;
        if (other != null) {
          hadErrors.set(true);
          throw new RuntimeException("Wtf? two threads in a handler: "
              + other + " and " + Thread.currentThread());
        }
        foo = Thread.currentThread();
        
        if (stealingQueue.isEmpty()) {
          slave.finished();
        } else {
          String suiteName = stealingQueue.pop();
          slave.newSuite(suiteName);
        }
        
        foo = null;
      }
    });

    // stress.
    ExecutorService executor = Executors.newCachedThreadPool();
    final List<Callable<Void>> slaves = new ArrayList<>();
    for (int i = 0; i < randomIntBetween(1, 10); i++) {
      slaves.add(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          aggregatedBus.post(new SlaveIdle());
          return null;
        }
      });
    }
    for (Future<Void> f : executor.invokeAll(slaves)) {
      f.get();
    }
    executor.shutdown();
    
    assertFalse(hadErrors.get());
  }
}
