package com.carrotsearch.ant.tasks.junit4.listeners.antxml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Local markup to indicate which elements are extensions and where. 
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
@interface NotAnt {
  String extensionSource() default "junit4";
}
