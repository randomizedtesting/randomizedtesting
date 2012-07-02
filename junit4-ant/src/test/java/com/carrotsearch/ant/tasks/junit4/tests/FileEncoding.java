package com.carrotsearch.ant.tasks.junit4.tests;

import java.io.*;
import java.nio.charset.Charset;
import org.junit.Test;

public class FileEncoding {
  @Test
  public void checkFileEncoding() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    ps.println("unicode string=cześć, Привет, 今日は");
    ps.close();

    String encoding = System.getProperty("file.encoding");
    System.out.println("file.encoding=" + encoding);
    System.out.println("charset=" + Charset.defaultCharset());
    System.out.println("unicode string in " + encoding + "=cześć, Привет, 今日は");
    System.out.println("unicode string PrintStream'ed: " + baos.size() + " bytes, hex dump:");
    
    HexDump.dump(baos.toByteArray(), 0, System.out, 0);
  }
}
