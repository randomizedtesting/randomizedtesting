package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.carrotsearch.randomizedtesting.ClassValidator;

/**
 * If a type is annotated with {@link ClassValidators}, the validators are applied
 * to the type prior to executing any hooks or methods on that type.
 * 
 * <p>This allows custom validation strategies, such as preventing method overrides or shadowing,
 * for example (which may lead to confusion).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface ClassValidators {
  /**
   * An array of validator classes. These classes must be instantiable (public, static, no-args
   * constructor, etc.).
   */
  Class<? extends ClassValidator>[] value();
}
