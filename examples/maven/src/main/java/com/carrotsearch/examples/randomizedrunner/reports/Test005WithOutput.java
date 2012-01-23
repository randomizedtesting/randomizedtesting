package com.carrotsearch.examples.randomizedrunner.reports;

import org.junit.Test;


/**
 * A suite of nested test classes.
 */
public class Test005WithOutput {
  @Test
  public void stdout() {
    System.out.print("stdout-noeol");
  }
  
  @Test
  public void stdout_eol() {
    System.out.print("stdout-witheol\n");
  }
  
  @Test
  public void stderr() {
    System.err.print("stderr-noeol");
  }  

  @Test
  public void stderr_eol() {
    System.err.print("stderr-witheol\n");
  }  

  @Test
  public void stderr_stdout_interwoven() {
    System.out.print("stdout-begin-");
    System.out.flush();
    System.err.print("stderr-begin-");
    System.err.flush();
    System.out.print("stdout-end");
    System.out.flush();
    System.err.print("stderr-end");
    System.err.flush();
  }

  @Test
  public void longline() {
    for (int i = 0; i < 1000; i++) {
      System.out.print((i % 10) + '0');
    }
    System.out.println("... and done.");
  }
}
