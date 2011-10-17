package com.carrotsearch.randomizedtesting;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class TestUncaughtExceptionHandler extends WithNestedTestClass {
  public static class Nested extends RandomizedTest {
    @Test
    public void checkGlobal() throws Exception {
      assumeRunningNested();

      ThreadGroup parentTg = Thread.currentThread().getThreadGroup();
      while (parentTg.getParent() != null)
        parentTg = parentTg.getParent();

      final Thread t = new Thread(parentTg, "yoda") {
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
    Logger logger = Logger.getLogger(RandomizedRunner.class.getSimpleName());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamHandler handler = new StreamHandler(out, new SimpleFormatter());
    handler.setEncoding("UTF-8");
    try {
      logger.addHandler(handler);
      Result r = JUnitCore.runClasses(Nested.class);
      handler.flush();
      String messages = new String(out.toByteArray(), "UTF-8");
      Assert.assertTrue(messages.contains("yoda died."));      
      Assert.assertEquals(0, r.getFailureCount());
    } finally {
      logger.removeHandler(handler);
    }
  }
}
