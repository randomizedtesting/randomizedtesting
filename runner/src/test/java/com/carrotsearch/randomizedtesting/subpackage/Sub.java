package com.carrotsearch.randomizedtesting.subpackage;

import com.carrotsearch.randomizedtesting.TestMethodCollector;

@SuppressWarnings("all")
public class Sub extends TestMethodCollector.Super {
  public    void publicMethod() {}
  protected void protectedMethod() {}
            void packageMethod() {}
  private   void privateMethod() {}

  public    void openingProtectedMethod() {}

  public    static void staticPublicMethod() {}
  protected static void staticProtectedMethod() {}
            static void staticPackageMethod() {}
  private   static void staticPrivateMethod() {}
}
