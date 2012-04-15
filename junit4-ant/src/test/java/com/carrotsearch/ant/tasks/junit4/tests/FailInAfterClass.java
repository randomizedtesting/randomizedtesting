package com.carrotsearch.ant.tasks.junit4.tests;

import org.junit.AfterClass;
import org.junit.Test;

public class FailInAfterClass {
  public final static String MESSAGE = "This is @AfterClass output.";
  
  @Test
  public void dummy() {}

  @AfterClass
  public static void afterClass() {
    System.out.println(MESSAGE);
    throw new RuntimeException();
  }
}
