package com.carrotsearch.randomizedtesting;

import java.io.PrintStream;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class PrintEventListener extends RunListener {
  private final PrintStream out;
  private AtomicInteger assumptions = new AtomicInteger();

  public PrintEventListener(PrintStream out) {
    this.out = out;
  }
  
  @Override
  public void testRunStarted(Description description) throws Exception {
    out.println("Run started.");
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    out.println(String.format(Locale.ROOT, 
        "Run finished: run=%s, ignored=%s, failures=%s, assumptions=%s\n", 
        result.getRunCount(),
        result.getIgnoreCount(),
        result.getFailureCount(),
        assumptions.get()));
  }

  @Override
  public void testStarted(Description description) throws Exception {
    out.println("Started : " + description.getMethodName());
  }

  @Override
  public void testFinished(Description description) throws Exception {
    out.println("Finished: " + description.getMethodName());
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    out.println("Failure : " + failure);
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    out.println("Assumpt.: " + failure);
    assumptions.incrementAndGet();
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    out.println("Ignored : " + description.getMethodName());
  }
}