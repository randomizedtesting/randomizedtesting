package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface ThreadLeakAction {
  public static enum Action {
    /** Emit a warning using Java's logging system. */
    WARN,

    /** Try to {@link Thread#interrupt()} any leaked threads. */
    INTERRUPT;
  };

  Action [] value() default { Action.WARN, Action.INTERRUPT };
}