package com.carrotsearch.ant.tasks.junit4.tests;

public class Crash {
  public static native void crashMe();

  public static void loadLibrary() {
    StringBuilder b = new StringBuilder("Could not link with crashlib.");
    try {
      System.loadLibrary("crash");
      return;
    } catch (UnsatisfiedLinkError e) {
      b.append("\n").append(e.toString());
    }

    try {
      System.loadLibrary("crash64");
      return;
    } catch (UnsatisfiedLinkError e) {
      b.append("\n").append(e.toString());
    }

    throw new UnsatisfiedLinkError(b.toString());
  }
}
