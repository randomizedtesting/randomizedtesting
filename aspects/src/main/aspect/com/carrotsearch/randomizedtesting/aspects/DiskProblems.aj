package com.carrotsearch.randomizedtesting.aspects;

import java.io.IOException;
import java.util.Random;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.Rethrow;
import com.carrotsearch.randomizedtesting.generators.RandomInts;

import org.junit.Test;

/**
 * Mock some I/O errors.
 */
public aspect DiskProblems {
  /** Opening of file streams (simplified). */
  pointcut openFile(): 
        call(java.io.FileInputStream.new(..)) ||
        call(java.io.RandomAccessFile.new(..));
  
  /** Reading from any stream. */
  pointcut readStream(): 
        call(* java.io.InputStream+.read*(..));
  
  /** All test methods. */
  pointcut testMethods(): execution(@Test * *());
  
  /** Execution under a test method only. */
  pointcut testingThread(): cflowbelow(testMethods());
  
  /**
   * Simulate rare, random I/O error on FileInputStream opening.
   */
  Object around() : openFile() && testingThread() {
    RandomizedContext ctx = RandomizedContext.current();
    // 25% I/O errors rate.
    if (ctx.getRandom().nextInt(100) < 25) {
      Rethrow.rethrow(new IOException(
          "Simulated I/O exception on file opening."));
    }
    return proceed();
  }
  
  /**
   * Simulate rare, slow, but working disk (slow down reading).
   */
  before() : readStream() && testingThread() {
    RandomizedContext ctx = RandomizedContext.current();
    Random r = ctx.getRandom();
    // 25% I/O errors rate.
    if (r.nextInt(100) < 25) {
      try {
        Thread.sleep(RandomInts.randomIntBetween(r, 250, 1500));
      } catch (InterruptedException e) {
        // Nothing.
      }
    }
  }
}
