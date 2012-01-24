package com.carrotsearch.randomizedtesting;

import java.lang.annotation.Annotation;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;

/**
 * Runtime information about a {@link TestGroup}.
 */
public final class RuntimeTestGroup {

  /** The annotation marked as a group. */
  private final Annotation annotation;
  
  /** Group information. */
  private final TestGroup testGroup;

  /** @see #getName() */
  private final String name;
  
  /** @see #getSysPropertyName() */
  private final String sysProperty;

  /**
   * Execution state for the tests marked with this group. 
   */
  private boolean enabled;

  /**
   * Hide from the public.
   */
  RuntimeTestGroup(Annotation ann) {
    this.annotation = ann;
    this.name = getGroupName(ann.annotationType());
    this.sysProperty = getGroupSysProperty(ann.annotationType());
    this.testGroup = ann.annotationType().getAnnotation(TestGroup.class);
    
    try {
      this.enabled = RandomizedTest.systemPropertyAsBoolean(
          getSysPropertyName(), testGroup.enabled());
    } catch (IllegalArgumentException e) {
      // Ignore malformed system property, disable the group if malformed though.
      this.enabled = false;
    }
  }

  /**
   * Return the group's annotation.
   */
  public Annotation getAnnotation() {
    return annotation;
  }
  
  /** 
   * Test group name, resolving defaults.
   * @see TestGroup#name()
   */
  public String getName() {
    return name;
  }
  
  /** 
   * Test group system property, resolving defaults.
   * @see TestGroup#sysProperty()
   */
  public String getSysPropertyName() {
    return sysProperty;
  }

  /**
   * Returns the execution state for this group.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Return {@link TestGroup}'s name assigned to a given annotation.
   */
  public static String getGroupName(Class<? extends Annotation> annotationClass) {
    TestGroup testGroup = annotationClass.getAnnotation(TestGroup.class);
    if (testGroup == null)
      throw new IllegalArgumentException("Annotation must have a @TestGroup annotation: " 
          + annotationClass);

    String tmp = RandomizedRunner.emptyToNull(testGroup.name());
    return tmp == null ? annotationClass.getSimpleName().toLowerCase() : tmp;
  }

  /**
   * Return {@link TestGroup}'s system property assigned to a given annotation.
   */
  public static String getGroupSysProperty(Class<? extends Annotation> annotationClass) {
    TestGroup testGroup = annotationClass.getAnnotation(TestGroup.class);

    String tmp = RandomizedRunner.emptyToNull(testGroup.sysProperty());
    return (tmp != null ? tmp : SysGlobals.prefixProperty(getGroupName(annotationClass))); 
  }
}
