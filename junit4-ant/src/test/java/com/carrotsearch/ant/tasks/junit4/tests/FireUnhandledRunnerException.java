package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.Test;

import com.carrotsearch.ant.tasks.junit4.slave.SlaveMain;

public class FireUnhandledRunnerException {
  public static final String EXCEPTION_MESSAGE = "BAMBOOOOOCHA!";

  @Test
  public void polluteRunner() {
    System.setProperty(SlaveMain.SYSPROP_FIRERUNNERFAILURE, EXCEPTION_MESSAGE);
  }
}
