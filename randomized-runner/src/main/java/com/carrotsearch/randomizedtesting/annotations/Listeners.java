package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import com.carrotsearch.randomizedtesting.RandomizedRunner;

/**
 * Annotate your suite class with this annotation to automatically add hooks to
 * the {@link RunNotifier} used for executing tests inside
 * {@link RandomizedRunner}.
 * 
 * @see #value() 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Listeners {
  /**
   * An array of listener classes. These classes must be instantiable (public, static, no-args
   * constructor, etc.).
   */
  Class<? extends RunListener>[] value();
}
