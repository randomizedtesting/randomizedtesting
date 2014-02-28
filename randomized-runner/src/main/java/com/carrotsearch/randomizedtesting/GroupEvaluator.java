package com.carrotsearch.randomizedtesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import com.carrotsearch.randomizedtesting.RandomizedRunner.TestCandidate;

/**
 * Evaluates enabled/ disabled state for a given test group.
 */
class GroupEvaluator {
  GroupEvaluator(List<TestCandidate> testCandidates) {
    // XXX: collect all test groups.
  }

  void appendGroupFilteringOptions(ReproduceErrorMessageBuilder reproduceErrorMessageBuilder) {
    /*
    for (RuntimeTestGroup g : ctx.getTestGroups().values()) {
      String sysPropName = g.getSysPropertyName();
      if (System.getProperty(sysPropName) != null) {
        appendOpt(sysPropName, System.getProperty(sysPropName));
      }
    }
    */
  }

  String isIgnored(AnnotatedElement... elements) {
    /*
    for (AnnotatedElement element : Arrays.asList(c.method, suiteClass)) {
      for (Annotation ann : element.getAnnotations()) {
      }
    }

    // XXX: testGroupEvaluator.evaluate(annotationsOf(c.method, suiteClass), ruleChain)
    // the default should be that the test runs only if all test groups are enabled.
    // tests.groups=foo,bar -> foo and bar
    // tests.groups=foo,!bar -> foo and not bar
    // tests.groups=*,!foo -> all except foo

    // XXX: msg:= "'" + g.getName() + "' test group is disabled (" + toString(g.getAnnotation()) + ")"
     */
    return null;
  }

  boolean isEnabled(Class<? extends Annotation> annotation) {
    // XXX: evaluate whether a single test group is enabled.
    return false;
  }  
}

/*
private HashMap<Class<? extends Annotation>, RuntimeTestGroup> collectGroups(
    List<TestCandidate> testCandidates) {
  final HashMap<Class<? extends Annotation>, RuntimeTestGroup> groups = 
      new HashMap<Class<? extends Annotation>, RuntimeTestGroup>();

  // Always use @Nightly as a group.
  groups.put(Nightly.class, new RuntimeTestGroup(defaultNightly));

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

  // Check all annotations. 
  for (Annotation ann : annotations) {
    if (!groups.containsKey(ann) 
        && ann.annotationType().isAnnotationPresent(TestGroup.class)) {
      groups.put(ann.annotationType(), new RuntimeTestGroup(ann));
    }
  }

  return groups;
}


private String toString(Annotation ann) {
  if (ann == null) return "@null?";
  return ann.toString().replace(
      ann.annotationType().getName(), 
      ann.annotationType().getSimpleName());
}


  try {
    this.enabled = RandomizedTest.systemPropertyAsBoolean(
        getSysPropertyName(), testGroup.enabled());
  } catch (IllegalArgumentException e) {
    // Ignore malformed system property, disable the group if malformed though.
    this.enabled = false;
  }
*/
