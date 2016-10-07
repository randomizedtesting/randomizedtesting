package com.carrotsearch.randomizedtesting;

import java.util.Locale;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class PrintEventListener extends RunListener {
  @Override
  public void testRunStarted(Description description) throws Exception {
    System.out.println("Run started.");
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    System.out.println(String.format(Locale.ROOT, 
        "Run finished: run=%s, ignored=%s, failures=%s\n", 
        result.getRunCount(),
        result.getIgnoreCount(),
        result.getFailureCount()));
  }

  @Override
  public void testStarted(Description description) throws Exception {
    System.out.println("Started : " + description.getMethodName());
  }

  @Override
  public void testFinished(Description description) throws Exception {
    System.out.println("Finished: " + description.getMethodName());
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    System.out.println("Failure : " + failure);
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    System.out.println("Assumpt.: " + failure);
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    System.out.println("Ignored : " + description.getMethodName());
  }
}