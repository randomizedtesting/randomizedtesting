package com.carrotsearch.randomizedtesting.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface ThreadLeakGroup {
  public static enum Group {
    /**
     * All JVM threads will be tracked. 
     * 
     * <p>WARNING: This option will not work
     * on IBM J9 because of livelock bugs in {@link Thread#getAllStackTraces()}.
     */
    ALL,

    /**
     * The "main" thread group and descendants will be tracked.   
     */
    MAIN, 

    /** 
     * Only per-suite test group and descendants will be tracked. 
     */
    TESTGROUP
  }

  Group value() default Group.MAIN;
}