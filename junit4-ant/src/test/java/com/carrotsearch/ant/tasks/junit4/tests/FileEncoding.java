package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.junit.Test;

public class FileEncoding {
  @Test
  public void checkFileEncoding() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ps.println("unicode string=cześć, Привет, 今日は");
    ps.close();

    System.out.println("file.encoding=" + System.getProperty("file.encoding"));
    System.out.println("charset=" + Charset.defaultCharset());
    System.out.println("unicode string=cześć, Привет, 今日は");
    System.out.println("unicode string byte length: " + baos.size());
  }
}
