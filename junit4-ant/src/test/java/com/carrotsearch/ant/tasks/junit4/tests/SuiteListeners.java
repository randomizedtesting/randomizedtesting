package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Listeners;

import static org.junit.Assert.*;

@Listeners({
  SuiteListeners.PrintEventListener.class
})
public class SuiteListeners extends RandomizedTest {
  
  public static class PrintEventListener extends RunListener {

    @Override
    public void testRunStarted(Description description) throws Exception {
      System.out.println("testRunStarted: " + description);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
      System.out.println("testRunFinished.");
    }

    @Override
    public void testStarted(Description description) throws Exception {
      System.out.println("testStarted: " + description);
    }

    @Override
    public void testFinished(Description description) throws Exception {
      System.out.println("testFinished: " + description);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
      System.out.println("testFailure: " + failure);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
      System.out.println("testAssumptionFailure: " + failure);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
      System.out.println("testIgnored: " + description);
    }
  }

  @Test 
  public void passing() {}
  
  @Test @Ignore  
  public void ignored() {}
  
  @Test 
  public void aignored() { assumeTrue(false); }

  @Test 
  public void failure() { fail(); }
}
