package com.carrotsearch.ant.tasks.junit4.spikes;

import java.io.RandomAccessFile;
import java.util.Date;


@SuppressWarnings("resource")
public class LocalRun {
  public static void main(String[] args) throws Exception {
    System.out.println("Live and prosper.");
    
    // make sure to delete raf first.
    final RandomAccessFile raf = new RandomAccessFile("shared.log", "rw");
    final RandomAccessFile raf2 = new RandomAccessFile("shared.log", "r");
    
    Thread t1 = new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            int c = raf2.read();
            if (c == -1) {
              Thread.sleep(500);
              continue;
            }
            
            System.out.write(c);
            System.out.flush();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    };
    
    Thread t2 = new Thread() {
      @Override
      public void run() {
        try {
          while (true) {
            raf.write((new Date().toString() + "\n").getBytes("UTF-8"));
            Thread.sleep(2000);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    
    t1.start();
    t2.start();
  }
}
