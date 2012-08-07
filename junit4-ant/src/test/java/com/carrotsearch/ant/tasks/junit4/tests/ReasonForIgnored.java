package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.carrotsearch.ant.tasks.junit4.it.TestTextReport;
import com.carrotsearch.randomizedtesting.RandomizedRunner;

/**
 * Holder class for tests used in {@link TestTextReport#reasonForIgnored()}.
 */
public class ReasonForIgnored
{
    public static class IgnoredMethods {
        @Test
        @Ignore
        public void simplyIgnored() {
        }

        @Test
        @Ignore("Ignored method.")
        public void ignoredWithRationale() {
        }
    }

    @Ignore("Ignored class.")
    public static class IgnoredClass {
        @Test
        public void ignoredByClass() {
            System.out.println("Hello!");
        }
    }

    @RunWith(RandomizedRunner.class)
    public static class IgnoredMethodsRR extends IgnoredMethods {
      @Test
      @Ignore("Ignored method (2).")
      public void ignoredAgain() {
      }
    }
    
    @Ignore("Ignored class.")
    @RunWith(RandomizedRunner.class)
    public static class IgnoredClassRR extends IgnoredClass {
    }

    @RunWith(RandomizedRunner.class)
    public static class IgnoredGroup  {
        @DisabledGroup @Test
        public void ignoredByGroup() {}
    }
}
