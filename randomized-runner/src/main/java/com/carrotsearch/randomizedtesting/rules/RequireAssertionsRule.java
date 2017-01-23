package com.carrotsearch.randomizedtesting.rules;

import java.util.Objects;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.ClassRule;
import org.junit.rules.TestRule;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.SysGlobals;
import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;

/**
 * Require assertions {@link TestRule}.
 * 
 * @see ClassRule
 */
public class RequireAssertionsRule extends TestRuleAdapter {
  public static final boolean TEST_ASSERTS_ENABLED = 
      RandomizedTest.systemPropertyAsBoolean(SysGlobals.SYSPROP_ASSERTS(), /* default to assertions required */ true);
  
  private final Class<?> targetClass;

  public RequireAssertionsRule(Class<?> targetClass) {
    this.targetClass = Objects.requireNonNull(targetClass);
  }

  @SuppressForbidden("Permitted sysout.")
  @Override
  protected void before() throws Throwable {
    // Make sure -ea matches -Dtests.asserts.
    boolean assertsEnabled = targetClass.desiredAssertionStatus();
    if (assertsEnabled != TEST_ASSERTS_ENABLED) {
      String msg = "Assertion state mismatch on " + targetClass.getSimpleName() + ": ";
      if (assertsEnabled) {
        msg += "-ea was specified";
      } else {
        msg += "-ea was not specified";
      }
      msg += " but -Dtests.asserts=" + TEST_ASSERTS_ENABLED;

      System.err.println(msg);
      throw new Exception(msg);
    }
  }
} 