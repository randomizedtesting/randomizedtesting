package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.File;

import org.junit.Test;

public class CwdConflict1 {
  @Test
  public void testCreateConflictingFile() throws Exception {
    File file = new File("cwdconflict.tmp");
    if (!file.createNewFile()) {
      throw new RuntimeException("File already exists.");
    }
  }
}
