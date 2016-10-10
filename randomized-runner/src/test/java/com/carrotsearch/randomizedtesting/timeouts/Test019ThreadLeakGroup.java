package com.carrotsearch.randomizedtesting.timeouts;

import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.Utils;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakGroup;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakGroup.Group;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;

/**
 * Checks thread leak detection group.
 */
public class Test019ThreadLeakGroup extends WithNestedTestClass {
  volatile static Thread t;

  @ThreadLeakScope(Scope.TEST)
  @ThreadLeakGroup(Group.TESTGROUP)
  public static class Nested1 extends RandomizedTest {
    @Test
    public void testLeakOutsideOfGroup() throws Exception {
      assumeRunningNested();

      final CountDownLatch latch = new CountDownLatch(1);
      ThreadGroup newTopGroup = new ThreadGroup(Utils.getTopThreadGroup(), "foobar-group");
      t = new Thread(newTopGroup, "foobar") {
        @Override
        public void run() {
          try {
            latch.countDown();
            Thread.sleep(5000);
          } catch (InterruptedException e) {}
        }
      };
      t.start();
      latch.await();
    }
  }

  @ThreadLeakGroup(Group.MAIN)
  public static class Nested2 extends Nested1 {
  }

  @ThreadLeakGroup(Group.ALL)
  public static class Nested3 extends Nested1 {
  }

  @Test
  public void testTestGroup() throws Throwable {
    FullResult r = runTests(Nested1.class);
    Assertions.assertThat(r.getFailures()).isEmpty();
    Assertions.assertThat(t != null && t.isAlive()).isTrue();
  }

  @Test
  public void testMainGroup() throws Throwable {
    FullResult r = runTests(Nested2.class);
    Assertions.assertThat(r.getFailures()).isEmpty();
    Assertions.assertThat(t != null && t.isAlive()).isTrue();
  }

  @Test
  public void testAll() throws Throwable {
    FullResult r = runTests(Nested3.class);
    Utils.assertNoLiveThreadsContaining("foobar");
    Utils.assertFailureWithMessage(r, "1 thread leaked from TEST");
  }

  @After
  public void cleanup() throws Exception {
    if (t != null) {
      t.interrupt();
      t.join();
      t = null;
    }
  }
}
