package com.carrotsearch.randomizedtesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.carrotsearch.randomizedtesting.RandomizedRunner.TestCandidate;
import com.carrotsearch.randomizedtesting.annotations.TestGroup;

/**
 * Evaluates enabled/ disabled state for a given test group.
 */
class GroupEvaluator {
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

  private final HashMap<Class<? extends Annotation>, TestGroupInfo> testGroups;

  GroupEvaluator(List<TestCandidate> testCandidates) {
    testGroups = collectGroups(testCandidates);
  }

  private HashMap<Class<? extends Annotation>, TestGroupInfo> collectGroups(List<TestCandidate> testCandidates) {
    final HashMap<Class<? extends Annotation>, TestGroupInfo> groups = new HashMap<Class<? extends Annotation>, TestGroupInfo>();

    // Collect all groups declared on methods and instance classes.
    HashSet<Class<?>> clazzes = new HashSet<Class<?>>();
    HashSet<Annotation> annotations = new HashSet<Annotation>();
    for (TestCandidate c : testCandidates) {
      final Class<?> testClass = c.instanceProvider.getTestClass();
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
  }

  String isTestIgnored(AnnotatedElement... elements) {
    for (AnnotatedElement element : elements) {
      for (Annotation ann : element.getAnnotations()) {
        if (ann.annotationType().isAnnotationPresent(TestGroup.class) 
            && !isTestGroupEnabled(ann.annotationType())) {
          TestGroupInfo g = testGroups.get(ann.annotationType());
          return "'" + g.name + "' test group is disabled (" + toString(ann) + ")";
        }
      }
    }

    // XXX: testGroupEvaluator.evaluate(annotationsOf(c.method, suiteClass), ruleChain)
    // the default should be that the test runs only if all test groups are enabled.
    // tests.groups=foo,bar -> foo and bar
    // tests.groups=foo,!bar -> foo and not bar
    // tests.groups=*,!foo -> all except foo

    // XXX: msg:= "'" + g.getName() + "' test group is disabled (" + toString(g.getAnnotation()) + ")"
    return null;
  }

  boolean isTestGroupEnabled(Class<? extends Annotation> ann) {
    if (!ann.isAnnotationPresent(TestGroup.class)) {
      throw new IllegalArgumentException("Expected an annotation annotated with @TestGroup: " + ann);
    }

    if (testGroups.containsKey(ann)) {
      return testGroups.get(ann).enabled;
    } else {
      TestGroupInfo testGroupInfo = new TestGroupInfo(ann);
      testGroups.put(ann, testGroupInfo);
      return testGroupInfo.enabled;
    }
  }
  
  private String toString(Annotation ann) {
    if (ann == null) return "@null?";
    return ann.toString().replace(
        ann.annotationType().getName(), 
        ann.annotationType().getSimpleName());
  }  
}
