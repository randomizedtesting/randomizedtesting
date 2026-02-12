package com.carrotsearch.randomizedtesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.carrotsearch.randomizedtesting.FilterExpressionParser.IContext;
import com.carrotsearch.randomizedtesting.FilterExpressionParser.Node;
import com.carrotsearch.randomizedtesting.RandomizedRunner.TestCandidate;
import com.carrotsearch.randomizedtesting.annotations.TestGroup;

/**
 * Evaluates enabled/ disabled state for a given test group.
 */
public final class GroupEvaluator {
  private static class TestGroupInfo {
    final TestGroup group;
    final String name;
    final String sysProperty;
    final boolean enabled;

    TestGroupInfo(Class<? extends Annotation> annType) {
      group = annType.getAnnotation(TestGroup.class); 
      name = TestGroup.Utilities.getGroupName(annType); 
      sysProperty = TestGroup.Utilities.getSysProperty(annType);

      boolean enabled;
      try {
        enabled = RandomizedTest.systemPropertyAsBoolean(sysProperty, group.enabled());
      } catch (IllegalArgumentException e) {
        // Ignore malformed system property, disable the group if malformed though.
        enabled = false;
      }
      this.enabled = enabled;
    }
  }

  private final Map<Class<? extends Annotation>, TestGroupInfo> testGroups;
  private final Node filter;
  private String filterExpression;

  GroupEvaluator(List<TestCandidate> testCandidates) {
    testGroups = new ConcurrentHashMap<>(collectGroups(testCandidates));

    filterExpression = System.getProperty(SysGlobals.SYSPROP_TESTFILTER());
    if (filterExpression != null && filterExpression.trim().isEmpty()) {
      filterExpression = null;
    }

    filter = filterExpression != null ? new FilterExpressionParser().parse(filterExpression) : null;
  }

  private HashMap<Class<? extends Annotation>, TestGroupInfo> collectGroups(List<TestCandidate> testCandidates) {
    final HashMap<Class<? extends Annotation>, TestGroupInfo> groups = new HashMap<Class<? extends Annotation>, TestGroupInfo>();

    // Collect all groups declared on methods and instance classes.
    HashSet<Class<?>> clazzes = new HashSet<Class<?>>();
    HashSet<Annotation> annotations = new HashSet<Annotation>();
    for (TestCandidate c : testCandidates) {
      final Class<?> testClass = c.getTestClass();
      if (!clazzes.contains(testClass)) {
        clazzes.add(testClass);
        annotations.addAll(Arrays.asList(testClass.getAnnotations()));
      }
      annotations.addAll(Arrays.asList(c.method.getAnnotations()));
    }

    // Get TestGroup annotated annotations. 
    for (Annotation ann : annotations) {
      Class<? extends Annotation> annType = ann.annotationType();
      if (!groups.containsKey(ann) && annType.isAnnotationPresent(TestGroup.class)) {
        groups.put(annType, new TestGroupInfo(annType));
      }
    }

    return groups;
  }

  void appendGroupFilteringOptions(ReproduceErrorMessageBuilder builder) {
    for (TestGroupInfo info : testGroups.values()) {
      if (System.getProperty(info.sysProperty) != null) {
        builder.appendOpt(info.sysProperty, System.getProperty(info.sysProperty));
      }
    }

    if (hasFilteringExpression()) {
      builder.appendOpt(SysGlobals.SYSPROP_TESTFILTER(), filterExpression);
    }
  }

  boolean hasFilteringExpression() {
    return filterExpression != null;
  }  

  /**
   * @return Returns a non-null string with the reason why the annotated element (class, test or test-class pair) 
   *         should be ignored in the execution. This is an expert-level method, typically tests 
   *         shouldn't be concerned with this.
   */
  public String getIgnoreReason(AnnotatedElement... elements) {
    final Map<String, Annotation> annotations = new HashMap<String,Annotation>();

    for (AnnotatedElement element : elements) {
      for (Annotation ann : element.getAnnotations()) {
        Class<? extends Annotation> annType = ann.annotationType();
        if (annType.isAnnotationPresent(TestGroup.class)) {
          TestGroupInfo testGroupInfo = testGroups.computeIfAbsent(annType, k -> new TestGroupInfo(annType));
          annotations.put(testGroupInfo.name, ann);
        }
      }
    }

    String defaultState = null;
    for (Annotation ann : annotations.values()) {
      TestGroupInfo g = testGroups.get(ann.annotationType());
      if (!g.enabled) {
        defaultState = "'" + g.name + "' test group is not enabled (annotation: "
                       + toString(ann) + ", sys property: " + g.sysProperty + ")";
        break;
      }
    }

    if (hasFilteringExpression()) {
      final String defaultStateCopy = defaultState;
      boolean enabled = filter.evaluate(new IContext() {
        @Override
        public boolean defaultValue() {
          return defaultStateCopy == null;
        }

        @Override
        public boolean hasGroup(String value) {
          if (value.startsWith("@")) value = value.substring(1);
          for (Annotation ann : annotations.values()) {
            if (value.equalsIgnoreCase(testGroups.get(ann.annotationType()).name)) {
              return true;
            }
          }
          return false;
        }
      });
      return enabled ? null : "Test filter condition: " + filterExpression;
    } else {
      return defaultState;
    }
  }
  
  private String toString(Annotation ann) {
    if (ann == null) return "@null?";
    return ann.toString().replace(
        ann.annotationType().getName(), 
        ann.annotationType().getSimpleName());
  }

  /**
   * @return Returns the current state of an annotation marked with
   *         {@link TestGroup}. Note that tests may be enabled or disabled using filtering
   *         expressions so an "enabled" group does not necessarily mean a test marked with
   *         this group will be executed.
   */
  public boolean isGroupEnabled(Class<? extends Annotation> testGroupAnnotation) {
    TestGroupInfo testGroupInfo = testGroups.computeIfAbsent(testGroupAnnotation, k -> {
      if (!testGroupAnnotation.isAnnotationPresent(TestGroup.class)) {
        throw new IllegalArgumentException("This annotation is not marked with @"
                + TestGroup.class.getName() + ": " + testGroupAnnotation.getName());
      }

      return new TestGroupInfo(testGroupAnnotation);
    });

    return testGroupInfo.enabled;
  }
}
