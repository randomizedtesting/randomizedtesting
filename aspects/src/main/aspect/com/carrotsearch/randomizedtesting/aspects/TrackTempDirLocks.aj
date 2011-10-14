package com.carrotsearch.randomizedtesting.aspects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.Rethrow;

/**
 * Track temporary folder requests, their location and if they cannot be
 * released, fail the test case with the info where the allocation took place.
 */
public aspect TrackTempDirLocks {
  /** Request for a temporary folder. */
  pointcut newTempDir(): 
        call(static File RandomizedTest.newTempDir());
  
  /** All test methods. */
  pointcut testMethods(): execution(@Test * *());
  
  /** Execution under a test method only. */
  pointcut testingThread(): cflowbelow(testMethods());
  
  private final IdentityHashMap<File,StackTraceElement[]> stacks = new IdentityHashMap<File,StackTraceElement[]>();
  
  private final IdentityHashMap<Thread,List<File>> openDirs = new IdentityHashMap<Thread,List<File>>();

  /**
   * Remember when a temporary folder is requested.
   */
  File around() : newTempDir() && testingThread() {
    File dir = proceed();
    System.out.println("Allocated: " + dir);
    synchronized (stacks) {
      Thread current = Thread.currentThread();
      stacks.put(dir, current.getStackTrace());
      if (!openDirs.containsKey(current)) {
        openDirs.put(current, new ArrayList<File>());        
      }
      openDirs.get(current).add(dir);
    }
    return dir;
  }
  
  /**
   * After a test method exits, check if the temporary folder can be removed. If
   * it is locked, dump the info and fail the test. It is assumed test methods
   * don't recurse into each other.
   */
  after() : testMethods() {
    synchronized (stacks) {
      List<File> dirs = openDirs.remove(Thread.currentThread());
      if (dirs == null) return;
      for (File dir : dirs) {
        try {
          forceDeleteRecursively(dir);
          if (dir.exists()) {
            IOException e = new IOException(
                "A thread left locked temporary folder: " + dir +
                ", allocation stack below.");
            e.setStackTrace(stacks.get(dir));
            throw e;
          }
        } catch (IOException e) {
          Rethrow.rethrow(e);
        }
      }
    }
  }
  
  protected static void forceDeleteRecursively(File fileOrDir) throws IOException {
    if (fileOrDir.isDirectory()) {
      // Not a symlink? Delete contents first.
      if (fileOrDir.getCanonicalPath().equals(fileOrDir.getAbsolutePath())) {
        for (File f : fileOrDir.listFiles()) {
          forceDeleteRecursively(f);
        }
      }
    }
    
    fileOrDir.delete();
  }
}
