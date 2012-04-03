package com.carrotsearch.randomizedtesting;

import org.junit.runner.Description;

/**
 * A filter for {@link Description#getClassName()}.
 */
public class ClassGlobFilter extends GlobFilter {
  public ClassGlobFilter(String globPattern) {
    super(globPattern);
  }

  @Override
  public boolean shouldRun(Description description) {
    String className = description.getClassName();
    return className == null || globMatches(className);
  }

  @Override
  public String describe() {
    return "Class matches: " + globPattern;
  }
}
