package com.carrotsearch.ant.tasks.junit4.tests;

import java.lang.annotation.*;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@TestGroup(enabled = false)
public @interface DisabledGroup {
  /** Additional description, if needed. */
  String value() default "";
}
