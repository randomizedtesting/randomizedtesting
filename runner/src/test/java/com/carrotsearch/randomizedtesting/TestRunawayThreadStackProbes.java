package com.carrotsearch.randomizedtesting;

import java.util.Collections;
import java.util.List;
import java.util.logging.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeaks;
import com.carrotsearch.randomizedtesting.annotations.Timeout;
import com.google.common.collect.Lists;

public class TestRunawayThreadStackProbes extends WithNestedTestClass {
  @ThreadLeaks(stackSamples = 5)
  public static class Nested extends RandomizedTest {
    @Test
    public void leaveBehind() throws Exception {
      assumeRunningNested();
      Thread t = new Thread(new Runnable() {
        public void run() {
          List<String> input = Lists.newArrayList();
          for (int i = 0; i < 500; i++) input.add(randomAsciiOfLengthBetween(1, 10));

          while (!Thread.currentThread().isInterrupted()) {
            Collections.sort(input);
          }
        }
      });
      t.start();
    }
  }

  @ThreadLeaks(stackSamples = 5)
  public static class Nested2 extends RandomizedTest {
    @Test @Timeout(millis = 500)
    public void timeout() throws Exception{
      assumeRunningNested();
      List<String> input = Lists.newArrayList();
      for (int i = 0; i < 500; i++) input.add(randomAsciiOfLengthBetween(1, 10));

      while (!Thread.currentThread().isInterrupted()) {
        Collections.sort(input);
      }
    }
  }

  @Test
  public void checkThreadProbes() throws Throwable {
    final StringBuilder log = new StringBuilder();
    Handler handler = new Handler() {
      @Override
      public void publish(LogRecord record) {
        log.append(record.getMessage()).append("\n");
      }

      @Override
      public void flush() {}
      
      @Override
      public void close() throws SecurityException {}
    };

    RandomizedRunner.logger.addHandler(handler);
    RandomizedRunner.logger.setUseParentHandlers(false);
    try {
      Result r = JUnitCore.runClasses(Nested.class);
      Assert.assertThat(log.toString(), JUnitMatchers.containsString("5 stack trace probe(s) taken and the constant"));
      Assert.assertEquals(1, r.getFailureCount());
      log.setLength(0);
      
      r = JUnitCore.runClasses(Nested2.class);
      Assert.assertThat(log.toString(), JUnitMatchers.containsString("5 stack trace probe(s) taken and the constant"));
      Assert.assertEquals(1, r.getFailureCount());      
    } finally {
      RandomizedRunner.logger.removeHandler(handler);
      RandomizedRunner.logger.setUseParentHandlers(true);
    }
  }    
}
