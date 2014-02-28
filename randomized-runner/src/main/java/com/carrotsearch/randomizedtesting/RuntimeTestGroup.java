package com.carrotsearch.randomizedtesting;

import java.lang.annotation.Annotation;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;

/**
 * Runtime information about a {@link TestGroup}.
 */
public final class RuntimeTestGroup {

  /** The annotation marked as a group. */
  private final Annotation annotation;

  /** @see #getName() */
  private final String name;
  
  /** @see #getSysPropertyName() */
  private final String sysProperty;

  /**
   * Hide from the public.
   */
  RuntimeTestGroup(Annotation ann) {
    this.annotation = ann;
    this.name = getGroupName(ann.annotationType());
    this.sysProperty = getGroupSysProperty(ann.annotationType());
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
