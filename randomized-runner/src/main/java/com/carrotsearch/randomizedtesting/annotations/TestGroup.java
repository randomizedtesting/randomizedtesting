package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.SysGlobals;

/**
 * A test group applied to an annotation indicates that a given annotation
 * can be used on individual tests as "labels". The meaning of these labels is
 * mostly application-specific (example: {@link Nightly} which indicates slower, 
 * more intensive tests that are skipped during regular runs). 
 * 
 * <p>{@link RandomizedRunner} collects groups from all tests in a suite. A group
 * can be enabled or disabled using boolean system properties (or test 
 * hooks in the code). A test case is executed if it has no groups or if all of its groups
 * are enabled.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Inherited
public @interface TestGroup {
  /** 
   * The name of a test group. If not defined, the default (lowercased annotation
   * name) is used. 
   */
  String name() default "";

  /**
   * System property used to enable/ disable a group. If empty, a default is used:
   * <pre>
   * tests.<i>name</i>
   * </pre>
   */
  String sysProperty() default "";

  /**
   * Is the group enabled or disabled by default (unless overridden by test group filtering
   * rules).  
   */
  boolean enabled() default true;
  
  /**
   * Utilities to deal with annotations annotated with {@link TestGroup}.
   */
  public static class Utilities {
    public static String getGroupName(Class<? extends Annotation> annotationClass) {
      TestGroup testGroup = annotationClass.getAnnotation(TestGroup.class);
      if (testGroup == null)
        throw new IllegalArgumentException("Annotation must have a @TestGroup annotation: " 
            + annotationClass);

      String tmp = emptyToNull(testGroup.name());
      return tmp == null ? annotationClass.getSimpleName().toLowerCase(Locale.ROOT) : tmp;
    }

    public static String getSysProperty(Class<? extends Annotation> annotationClass) {
      TestGroup testGroup = annotationClass.getAnnotation(TestGroup.class);
      if (testGroup == null)
        throw new IllegalArgumentException("Annotation must have a @TestGroup annotation: " 
            + annotationClass);

      String tmp = emptyToNull(testGroup.sysProperty());
      return (tmp != null ? tmp : SysGlobals.prefixProperty(getGroupName(annotationClass))); 
    }
  
    private static String emptyToNull(String value) {
      if (value == null || value.trim().isEmpty())
        return null;
      return value.trim();
    }
  }
}
