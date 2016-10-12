package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Random;

import com.carrotsearch.randomizedtesting.RandomSupplier;
import com.carrotsearch.randomizedtesting.RandomizedContext;

/**
 * A supplier of {@link Random} instances for the {@link RandomizedContext}. The supplier class must declare
 * a public no-arg constructor.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TestContextRandomSupplier {
  Class<? extends RandomSupplier> value();
}
