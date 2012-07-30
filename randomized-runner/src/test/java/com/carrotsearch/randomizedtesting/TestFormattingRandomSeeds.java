package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.annotations.Repeat;

@RunWith(RandomizedRunner.class)
public class TestFormattingRandomSeeds {
  @Test
  public void minusOne() {
    check(-1L);
  }
  
  @Test
  public void zero() {
    check(0);
  }
  
  @Test
  public void maxLong() {
    check(Long.MAX_VALUE);
  }

  volatile static int progress = 0;

  @BeforeClass
  public static void beforeClass() {
    new Thread("dumper") {
      {
        setDaemon(true);
      }

      public void run() {
        try {
          while (true) {
            int p = progress;
            Thread.sleep(1000);
            if (p == progress) {
              Class<?> clazz = Class.forName("com.ibm.jvm.Dump");
              //Method m = clazz.getMethod("JavaDump");
              Method m = clazz.getMethod("SystemDump");
              m.invoke(null);

              System.out.println("Stopped.");
              Thread.getAllStackTraces();
              System.out.println("Or not.");
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }.start();
  }
  
  /** Heck, why not use ourselves here? ;) */
  @Test
  @Repeat(iterations = 1000)
  public void noise() throws Exception {
    progress++;
    check(RandomizedContext.current().getRandom().nextLong());
  }

  private void check(long seed) {
    String asString = SeedUtils.formatSeedChain(new Randomness(seed));
    Assert.assertEquals(seed, SeedUtils.parseSeedChain(asString)[0]);
  }
}
