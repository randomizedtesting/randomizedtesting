package com.carrotsearch.randomizedtesting.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.framework.AssertionFailedError;

import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that ensures static, reference fields of the suite class
 * (and optionally its superclasses) are cleaned up after a suite is completed.
 * This is helpful in finding out static memory leaks (a class references
 * something huge but is no longer used).
 * 
 * @see ClassRule
 * @see #accept(Field)
 */
public class StaticFieldsInvariantRule implements TestRule {
  public static final long DEFAULT_LEAK_THRESHOLD = 10 * 1024 * 1024;
  
  private final long leakThreshold;
  private final boolean countSuperclasses;
  
  /**
   * By default use {@link #DEFAULT_LEAK_THRESHOLD} as the threshold and count
   * in superclasses.
   */
  public StaticFieldsInvariantRule() {
    this(DEFAULT_LEAK_THRESHOLD, true);
  }
  
  public StaticFieldsInvariantRule(long leakThresholdBytes, boolean countSuperclasses) {
    this.leakThreshold = leakThresholdBytes;
    this.countSuperclasses = countSuperclasses;
  }
  
  static class Entry implements Comparable<Entry> {
    final long ramUsed;
    final Field field;
    
    public Entry(Field field, long ramUsed) {
      this.field = field;
      this.ramUsed = ramUsed;
    }
    
    @Override
    public int compareTo(Entry o) {
      if (this.ramUsed > o.ramUsed) return -1;
      if (this.ramUsed < o.ramUsed) return 1;
      return this.field.toString().compareTo(o.field.toString());
    }
  }
  
  @Override
  public Statement apply(final Statement s, final Description d) {
    return new StatementAdapter(s) {
      @Override
      protected void afterAlways(List<Throwable> errors) throws Throwable {
        ArrayList<Entry> fields = new ArrayList<Entry>();
        long ramEnd = 0;
        for (Class<?> c = d.getTestClass(); countSuperclasses && c.getSuperclass() != null; c = c.getSuperclass()) {
          for (Field field : c.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && 
                !field.getType().isPrimitive() &&
                accept(field)) {
              field.setAccessible(true);
              final long fieldRam = RamUsageEstimator.sizeOf(field.get(null));
              if (fieldRam > 0) {
                fields.add(new Entry(field, fieldRam));
                ramEnd += fieldRam;
              }
            }
          }
        }

        if (ramEnd > leakThreshold) {
          Collections.sort(fields);
          
          StringBuilder b = new StringBuilder();
          b.append(String.format(Locale.ENGLISH, "Clean up static fields (in @AfterClass?), "
              + "your test seems to hang on to approximately %,d bytes (threshold is %,d):",
              ramEnd, leakThreshold));
          
          for (Entry e : fields) {
            b.append(String.format(Locale.ENGLISH, "\n  - %,d bytes, %s", e.ramUsed,
                e.field.toString()));
          }
          
          errors.add(new AssertionFailedError(b.toString()));
        }
      }
    };
  }

  /**
   * @return Return <code>false</code> to exclude a given field from being
   *         counted. By default final fields are rejected.
   */
  protected boolean accept(Field field) {
    return !Modifier.isFinal(field.getModifiers());
  }
}