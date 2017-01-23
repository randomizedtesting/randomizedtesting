package com.carrotsearch.ant.tasks.junit4;

public enum NonEmptyWorkDirectoryAction {
  /** Ignore any existing files in the work directory. */
  IGNORE,
  
  /** 
   * Wipe the content of the work directory clean if there are any files in it.
   * Failure to delete any of the files will cause a build error.
   */
  WIPE,
  
  /**
   * Fail the build if the work directory is not empty. 
   */
  FAIL;
}
