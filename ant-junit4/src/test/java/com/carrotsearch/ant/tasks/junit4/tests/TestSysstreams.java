package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

public class TestSysstreams {
  @Test
  public void ok() {}

  @Test
  public void ok_sysout_syserr() {
    System.out.print("sysout");
    System.out.flush();
    System.err.print("syserr");
    System.err.flush();
    System.out.print("-sysout-contd.");
    System.out.flush();
    System.err.print("-syserr-contd.");
    System.err.flush();
  }
}
