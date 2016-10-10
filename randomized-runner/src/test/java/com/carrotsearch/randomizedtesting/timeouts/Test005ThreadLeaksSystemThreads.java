package com.carrotsearch.randomizedtesting.timeouts;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.WithNestedTestClass;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakGroup;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakGroup.Group;

/**
 * Checks if known demon threads spawned by certain library methods are properly
 * handled.
 */
public class Test005ThreadLeaksSystemThreads extends WithNestedTestClass {

  @ThreadLeakGroup(Group.MAIN)
  public static class Nested extends RandomizedTest {
    @Test
    public void tokenPoller() throws Exception {
      assumeRunningNested();
      try {
        MessageDigest instance = MessageDigest.getInstance("MD5");
        instance.update(randomByte());
        instance.digest();
      } catch (NoSuchAlgorithmException e) {
        Logger.getAnonymousLogger().log(Level.SEVERE, "No MD5 in MessageDigest?", e);
      }
    }
    
    @Test
    public void gcDaemon() throws Exception {
      assumeRunningNested();

      try {
        Class<?> clazz = Class.forName("sun.misc.GC");
        Method method = clazz.getDeclaredMethod("requestLatency", new Class[] {long.class});
        method.invoke(null, Long.valueOf(3600000));
      } catch (ClassNotFoundException e) {
        // Ignore, must be running under a JVM without this class.
      }
    }
  }
  
  @Test
  public void leftOverThread() throws Throwable {
    FullResult r = runTests(Nested.class);
    Assertions.assertThat(r.getFailures()).isEmpty();

    Assertions.assertThat(getLoggingMessages())
      .doesNotContain("java.lang.Thread.sleep")
      .doesNotContain("Uncaught exception");    
  }
}
