package com.carrotsearch.randomizedtesting;

import org.junit.runner.Description;

/**
 * A filter for {@link Description#getMethodName()}.
 */
public class MethodGlobFilter extends GlobFilter {
  public MethodGlobFilter(String globPattern) {
    super(globPattern);
  }
  
  @Override
  public boolean shouldRun(Description description) {
    String methodName = description.getMethodName();
    return methodName == null || globMatches(methodName);
  }

  @Override
  public String describe() {
    return "Method matches: " + globPattern;
  }
}
