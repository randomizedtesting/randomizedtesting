package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.*;

import com.carrotsearch.randomizedtesting.SeedDecorator;

/**
 * Allows modifying the master seed (before the suite is started).
 * 
 * <p>Use this annotation when you want to perturb or modify the master seed. This may be
 * useful if there are decisions taken in static contexts of multiple suites. In such a case
 * these decisions would always be identical (because at static context level the seed is 
 * always derived from the same master). With a {@link SeedDecorator} one can perturb
 * the seed for every suite. 
 * 
 * <ul>
 * <li><b>Extra care should be used to make permutations consistent across different runs.</b></li>
 * <li><b>Seed decorators must be thread-safe, re-entrable, preferably unsynchronized and 
 * must never fail!</b></li>
 * </ul>
 *
 * @see #value() 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SeedDecorators {
  /**
   * 
   */
  Class<? extends SeedDecorator>[] value();
}
