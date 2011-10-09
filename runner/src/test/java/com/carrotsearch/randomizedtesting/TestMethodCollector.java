package com.carrotsearch.randomizedtesting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;
import com.carrotsearch.randomizedtesting.subpackage.Sub;


public class TestMethodCollector {
  @SuppressWarnings("all")
  public static class Super {
    public    void publicMethod() {}
    protected void protectedMethod() {}
              void packageMethod() {}
    private   void privateMethod() {}

    protected void openingProtectedMethod() {}

    public    static void staticPublicMethod() {}
    protected static void staticProtectedMethod() {}
              static void staticPackageMethod() {}
    private   static void staticPrivateMethod() {}
  }

  // Sub is in a different package.

  @SuppressWarnings("all")
  public static class SubSub extends Sub {
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

  @Test
  public void checkOverrideRemoval() throws Exception {
    List<List<Method>> methods = 
        MethodCollector.removeOverrides(
            MethodCollector.sort(
                MethodCollector.allDeclaredMethods(SubSub.class)));

    List<String> actual = new ArrayList<String>();
    for (List<Method> clazzLevel : methods) {
      String clazz = clazzLevel.get(0).getDeclaringClass().getSimpleName();
      for (Method m : clazzLevel) {
        actual.add(clazz + "." + m.getName());
      }
    }

    // Expecting sorted alphabetically within class.
    List<String> expected = Arrays.asList(
        "SubSub.openingProtectedMethod",
        "SubSub.packageMethod",
        "SubSub.privateMethod",
        "SubSub.protectedMethod",
        "SubSub.publicMethod",
        "SubSub.staticPackageMethod",
        "SubSub.staticPrivateMethod",
        "SubSub.staticProtectedMethod",
        "SubSub.staticPublicMethod",
        "Sub.packageMethod",
        "Sub.privateMethod",
        "Sub.staticPackageMethod",
        "Sub.staticPrivateMethod",
        "Sub.staticProtectedMethod",
        "Sub.staticPublicMethod",
        "Super.privateMethod",
        "Super.staticPackageMethod",
        "Super.staticPrivateMethod",
        "Super.staticProtectedMethod",
        "Super.staticPublicMethod"
    );
    Assert.assertEquals(expected, actual);
  }
}
