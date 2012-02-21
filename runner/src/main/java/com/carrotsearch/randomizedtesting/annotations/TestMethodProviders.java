package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.carrotsearch.randomizedtesting.TestMethodProvider;

/**
 * Test case method provider.
 * 
 * TODO: it would be nice to have an _instance_ provider as opposed to method provider,
 * but it's hellishly difficult to integrate with the rest of the infrastructure (seeds,
 * repetitions, etc.).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TestMethodProviders {
  Class<? extends TestMethodProvider>[] value();
}
