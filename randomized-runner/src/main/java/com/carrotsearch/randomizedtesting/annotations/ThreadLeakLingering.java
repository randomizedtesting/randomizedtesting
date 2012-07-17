package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Timer;
import java.util.concurrent.Executors;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface ThreadLeakLingering {
  /**
   * Time in millis to "linger" for any left-behind threads. If equals 0, there
   * is no waiting.
   * 
   * <p>
   * This is particularly useful if there's no way to {@link Thread#join()} and
   * wait for the potential forked threads to terminate. This is the case with
   * {@link Timer} or {@link Executors} for example.
   */
  int linger() default 0;
}