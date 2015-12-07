package com.carrotsearch.ant.tasks.junit4.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tools.ant.filters.TokenFilter;

import com.google.common.base.Joiner;

/**
 * Stack trace filtering.
 */
public class StackTraceFilter {
  /**
   * Default stack trace filters.
   * 
   * @see #setDefaults(boolean)
   */
  private static List<Pattern> defaultFilters = Arrays.asList(
      // junit4
      Pattern.compile("^(\\s+at )(org\\.junit\\.)"),
      Pattern.compile("^(\\s+at )(junit\\.framework\\.JUnit4TestAdapter)"),
      // sun/ reflection
      Pattern.compile("^(\\s+at )(sun\\.reflect\\.)"),
      Pattern.compile("^(\\s+at )(java\\.lang\\.reflect\\.Method\\.invoke\\()"),
      // randomizedtesting's own launcher.
      Pattern.compile("^(\\s+at )(com\\.carrotsearch\\.ant\\.tasks\\.junit4\\.slave\\.SlaveMain)"));
  
  /** 
   * Whether or not to use the default filters.
   */
  private boolean useDefaults = true;
  
  /**
   * Whether or not to use this filter (just in case somebody wanted to disable
   * it via a property).
   */
  private boolean enabled = true;

  /**
   * Custom filters (from ANT's own TokenFilter).
   */
  private List<TokenFilter.Filter> customFilters = new ArrayList<>();
  
  /**
   * Use default filters (JUnit, randomized testing, some of the reflection stuff). 
   */
  public void setDefaults(boolean useDefaults) {
    this.useDefaults = useDefaults;
  }

  /**
   * Disable or enable the filter. 
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  /** */
  public void addContainsString(TokenFilter.ContainsString filter) {
    addConfigured(filter);
  } 

  /** */
  public void addContainsRegex(TokenFilter.ContainsRegex filter) {
    addConfigured(filter);
  } 

  /**
   * Add a custom filter.
   */
  public void addConfigured(TokenFilter.Filter filter) {
    customFilters.add(filter);
  }
  
  public String apply(String trace) {
    if (!enabled) return trace;

    List<String> lines = Arrays.asList(trace.split("[\r\n]+"));
    List<String> out = new ArrayList<>();
    
    nextLine: for (String line : lines) {
      if (useDefaults) {
        for (Pattern p : defaultFilters) {
          if (p.matcher(line).find()) {
            continue nextLine;
          }
        }
      }

      for (TokenFilter.Filter customFilter : customFilters) {
        if (customFilter.filter(line) != null) {
          continue nextLine;
        }
      }
      
      out.add(line);
    }
    
    return Joiner.on("\n").join(out);
  }
}
