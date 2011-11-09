package com.carrotsearch.randomizedtesting.listeners;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.randomizedtesting.*;
import com.carrotsearch.randomizedtesting.annotations.Nightly;

/**
 * A {@link RunListener} that lists all test cases to standard output. For each test
 * case, its name and status is printed, along with the system error and system output
 * streams, each prepended with a prefix indicating the stream a line was emitted from.    
 */
public class VerboseTestInfoPrinter extends RunListener {
  private enum Status {
    OK, IGNORED, ERROR, FAILED, ASSUMPTION_IGNORED, UNDEFINED;

    public String toString() {
      if (this != ASSUMPTION_IGNORED) return super.toString();
      else return "A/IGNORED";
    }
  }

  private PrintStream output;
  
  /** Test's execution time. */
  private long startTime;
  
  /** Test execution status. */
  private Status status = Status.UNDEFINED;
  
  private AtomicBoolean outputEmitted;
  private PrintStream prevOut;
  private PrintStream prevErr;

  private int nameLength = 90;
  private String testNamePatt = "%s%-" + nameLength + "s ";
  
  private class SmartOutputStream extends PrefixedOutputStream {
    public SmartOutputStream(byte[] prefix, AtomicBoolean mutex, OutputStream sink) {
      super(prefix, mutex, sink);
    }

    protected void firstOutput(OutputStream sink) throws IOException {
      if (status != Status.UNDEFINED) {
        sink.write("...(output follows)\n".getBytes());
      }
    }
  }
  
  public VerboseTestInfoPrinter() {
    output = System.out;
  }
  
  @Override
  public void testRunStarted(Description description) throws Exception {
    output.println();
    output.println("Suite: " + description.getDisplayName());

    prevOut = System.out;
    prevErr = System.err;

    outputEmitted = new AtomicBoolean(false);
    System.setOut(new PrintStream(new SmartOutputStream("  1> ".getBytes(), outputEmitted, output)));
    System.setErr(new PrintStream(new SmartOutputStream("  2> ".getBytes(), outputEmitted, output)));
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    flushStreams();
    outputEmitted.set(false);

    System.setOut(prevOut);
    System.setErr(prevErr);
  }
  
  @Override
  public void testStarted(Description description) throws Exception {
    flushStreams();
    if (outputEmitted.get()) {
      output.println();
    }
    outputEmitted.set(false);

    String testName = description.getMethodName();
    if (testName.length() > nameLength) {
      testName = testName.substring(0, nameLength - 3) + "...";
    }
    output.print(String.format(Locale.ENGLISH, testNamePatt, " #", testName));
    output.flush();

    status = Status.OK;
    startTime = System.currentTimeMillis();
  }

  @Override
  public void testFinished(Description description) throws Exception {
    flushStreams();

    if (outputEmitted.get()) {
      // Align if anything's been written to syserr/sysout.
      output.print(String.format(Locale.ENGLISH, testNamePatt, "  ", ""));
    }

    double time = (System.currentTimeMillis() - startTime) / 1000.0D;
    output.println(String
        .format(Locale.ENGLISH, "%10s [%5.2fs]", status, time));
    output.flush();

    outputEmitted.set(false);
    status = Status.UNDEFINED;
  }

  @Override
  public void testAssumptionFailure(Failure failure) {
    status = Status.ASSUMPTION_IGNORED;
  }
  
  @Override
  public void testIgnored(Description description) throws Exception {
    status = Status.IGNORED;
  }

  @Override
  public void testFailure(Failure failure) throws Exception {
    if (status != Status.UNDEFINED) {
      status = Status.ERROR;  
    }

    final Description d = failure.getDescription();
    final StringBuilder b = new StringBuilder();
    b.append("TEST FAILED : ").append(d.getDisplayName()).append("\n");
    b.append("Message  : " + failure.getMessage() + "\n");
    b.append("Reproduce:");
    b.append(" -D").append(RandomizedRunner.SYSPROP_RANDOM_SEED).append("=")
        .append(RandomizedContext.current().getRunnerSeed());
    if (d.getClassName() != null) b.append(" -D")
        .append(RandomizedRunner.SYSPROP_TESTCLASS).append("=")
        .append(d.getClassName());
    if (d.getMethodName() != null) b.append(" -D")
        .append(RandomizedRunner.SYSPROP_TESTMETHOD).append("=")
        .append(RandomizedRunner.stripSeed(d.getMethodName()));

    final List<String> otherProps = new ArrayList<String>();
    otherProps.add(RandomizedRunner.SYSPROP_ITERATIONS);
    otherProps.add(RuntimeTestGroup.getGroupSysProperty(Nightly.class));

    for (String p : otherProps) {
      if (System.getProperty(p) != null) {
        b.append(" -D").append(p).append("=").append(System.getProperty(p));
      }
    }

    b.append("\n");
    b.append("Throwable: " + failure.getTrace());

    System.err.println(b.toString());
  }

  /**
   * Flush system output and system error, reset counters.
   */
  private void flushStreams() {
    System.out.flush();
    System.err.flush();
  }  
}
