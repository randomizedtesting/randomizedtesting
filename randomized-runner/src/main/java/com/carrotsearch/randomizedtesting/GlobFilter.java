package com.carrotsearch.randomizedtesting;

import java.util.regex.Pattern;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * A filter that matches something using globbing (*) pattern.
 */
public abstract class GlobFilter extends Filter {
  protected final String globPattern;
  private final Pattern pattern;

  public GlobFilter(String glob) {
    this.globPattern = glob;
    this.pattern = globToPattern(glob);
  }

  /**
   * Check if a given string matches the glob.
   */
  protected final boolean globMatches(String string) {
    boolean result = pattern.matcher(string).matches();
    return result;
  }

  /**
   * Simplified conversion to a regexp.
   */
  private Pattern globToPattern(String glob) {
    StringBuilder pattern = new StringBuilder("^");
    for (char c : glob.toCharArray()) {
      switch (c) {
        case '*':
          pattern.append(".*");
          break;
        case '?':
          pattern.append('.');
          break;
        case '.':
        case '$':
        case '-':
        case '{':
        case '}':
        case '[':
        case ']':
          pattern.append("\\");
          pattern.append(c);
          break;
        case '\\':
          pattern.append("\\\\");
          break;
        default:
          pattern.append(c);
      }
    }
    pattern.append('$');
    return Pattern.compile(pattern.toString());
  }
  
  @Override
  public abstract boolean shouldRun(Description description);
  
  @Override
  public abstract String describe();
}
