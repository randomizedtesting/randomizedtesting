package com.carrotsearch.randomizedtesting;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class TestUncaughtExceptionChildThread extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Test
    public void checkGlobal() throws Exception {
      assumeRunningNested();

      final Thread t = new Thread("yoda") {
        public void run() {
          throw new RuntimeException("yoda died.");
        }
      };

      t.start();
      t.join();
    }
  }

  @Test
  public void testUncaught() throws Exception {
    Logger logger = Logger.getLogger(RunnerThreadGroup.class.getSimpleName());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamHandler handler = new StreamHandler(out, new SimpleFormatter());
    handler.setEncoding("UTF-8");
    try {
      logger.addHandler(handler);
      Result r = JUnitCore.runClasses(Nested.class);
      handler.flush();
      String messages = new String(out.toByteArray(), "UTF-8");
      Assert.assertEquals(1, r.getFailureCount());
      Assert.assertTrue(messages.contains("yoda died."));      
    } finally {
      logger.removeHandler(handler);
    }
  }
}
