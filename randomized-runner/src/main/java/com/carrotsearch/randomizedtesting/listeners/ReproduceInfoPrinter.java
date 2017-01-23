package com.carrotsearch.randomizedtesting.listeners;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.carrotsearch.randomizedtesting.*;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;

/**
 * A {@link RunListener} that emits to {@link System#err} a string with command
 * line parameters allowing quick test re-run under ANT command line.     
 */
public class ReproduceInfoPrinter extends RunListener {
  @Override
  @SuppressForbidden("Legitimate use of syserr.")
  public void testFailure(Failure failure) throws Exception {
    // Ignore assumptions.
    if (failure.getException() instanceof AssumptionViolatedException) {
      return;
    }

    final Description d = failure.getDescription();
    final StringBuilder b = new StringBuilder();
    b.append("FAILURE  : ").append(d.getDisplayName()).append("\n");
    b.append("Message  : " + failure.getMessage() + "\n");
    b.append("Reproduce: ");
    new ReproduceErrorMessageBuilder(b).appendAllOpts(failure.getDescription());

    b.append("\n");
    b.append("Throwable:\n");
    if (failure.getException() != null) {
      TraceFormatting traces = new TraceFormatting();
      try {
        traces = RandomizedContext.current().getRunner().getTraceFormatting();
      } catch (IllegalStateException e) {
        // Ignore if no context.
      }
      traces.formatThrowable(b, failure.getException());
    }

    System.err.println(b.toString());
  }
}
