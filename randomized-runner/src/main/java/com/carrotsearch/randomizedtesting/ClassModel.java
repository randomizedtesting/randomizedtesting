package com.carrotsearch.randomizedtesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class model for a test suite. Builds relationship (overrides/ shadows) links 
 * for methods and fields. 
 */
public final class ClassModel {
  static final Comparator<Method> methodSorter = new Comparator<Method>() {
    @Override
    public int compare(Method o1, Method o2) {
      return o1.toGenericString().compareTo(
             o2.toGenericString());
    }
  };

  static final Comparator<Field> fieldSorter = new Comparator<Field>() {
    @Override
    public int compare(Field o1, Field o2) {
      return o1.toGenericString().compareTo(
             o2.toGenericString());
    }
  };

  private final LinkedHashMap<Method, MethodModel> methods;
  private final LinkedHashMap<Field, FieldModel> fields;

  public static enum Scope {
    PUBLIC,
    PROTECTED,
    PACKAGE,
    PRIVATE
  }

  abstract static class ClassElement<T extends Member, E extends ClassElement<T,E>> {
    private final int modifiers;
    private final Scope scope;

    public final T element;
    private E up;
    private E down;

    public ClassElement(T element) {
      this.element = element;
      this.modifiers = element.getModifiers();
      this.scope = getAccessScope(modifiers);
    }

    private static Scope getAccessScope(int modifiers) {
      if (Modifier.isPublic(modifiers)) return Scope.PUBLIC;
      if (Modifier.isProtected(modifiers)) return Scope.PROTECTED;
      if (Modifier.isPrivate(modifiers)) return Scope.PRIVATE;
      return Scope.PACKAGE;
    }
    
    void setDown(E down) { assert this.down == null; this.down = down; }
    void setUp(E up) { assert this.up == null; this.up = up; }

    public E getDown() { return down; }
    public E getUp() { return up;    }
    public Scope getAccessScope() { return scope; }

    abstract boolean overridesOrShadows(ClassElement<T,E> sub);
  }

  private static abstract class ModelBuilder<T_MEMBER extends Member, T_MODEL extends ClassElement<T_MEMBER,T_MODEL>> {
    final LinkedHashMap<T_MEMBER,T_MODEL> build(Class<?> clazz) {
      final LinkedHashMap<T_MEMBER,T_MODEL> elements = new LinkedHashMap<T_MEMBER,T_MODEL>();
      final Map<T_MODEL,List<T_MODEL>> tops = new HashMap<T_MODEL, List<T_MODEL>>();
      for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
        for (T_MEMBER m : members(c)) {
          final T_MODEL model = model(m);
          if (elements.put(m, model) != null) {
            throw new RuntimeException("Class element should not have duplicates in superclasses: " + m);
          }

          // Link up overridden/ shadowed methods to any current tops.
          if (model.getAccessScope() != Scope.PRIVATE) {
            List<T_MODEL> list = tops.get(model);
            if (list == null) {
              tops.put(model, list = new ArrayList<T_MODEL>());
            }
            for (Iterator<T_MODEL> i = list.iterator(); i.hasNext();) {
              T_MODEL sub = i.next();
              if (model.overridesOrShadows(sub)) {
                i.remove();
                sub.setUp(model);
                model.setDown(sub);
              }
            }
            list.add(model);
          }
        }
      }
      return elements;
    }

    protected abstract T_MEMBER[] members(Class<?> c);
    protected abstract T_MODEL model(T_MEMBER t);
  }

  public static final class MethodModel extends ClassElement<Method, MethodModel> {
    public MethodModel(Method m) {
      super(m);
    }

    @Override
    public String toString() {
      return element.getDeclaringClass().getSimpleName() + "." + element.getName();
    }
    
    @Override
    boolean overridesOrShadows(ClassElement<Method, MethodModel> sub) {
      final Method m1 = element;
      final Method m2 = sub.element;

      if (!m1.getName().equals(m2.getName())) {
        return false;
      }

      if (!Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())) {
        return false;
      }
      
      final Package package1 = m1.getDeclaringClass().getPackage();
      final Package package2 = m2.getDeclaringClass().getPackage();
      if (getAccessScope() == Scope.PACKAGE) {
        return package1.equals(package2);
      } else {
        return true;
      }
    }
    
    @Override
    public int hashCode() {
      int hashCode = element.getName().hashCode();
      for (Class<?> c : element.getParameterTypes()) {
        hashCode += 31 * c.hashCode();
      }
      return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
      MethodModel other = (MethodModel) obj;
      return element.getName().equals(other.element.getName()) &&
             Arrays.equals(element.getParameterTypes(),
                           other.element.getParameterTypes());
    }
  }

  public static final class FieldModel extends ClassElement<Field, FieldModel> {
    public FieldModel(Field f) {
      super(f);
    }

    @Override
    public String toString() {
      return element.getDeclaringClass().getSimpleName() + "." + element.getName();
    }

    @Override
    boolean overridesOrShadows(ClassElement<Field, FieldModel> sub) {
      final Field f1 = element;
      final Field f2 = sub.element;

      if (!f1.getName().equals(f2.getName())) {
        return false;
      }

      final Package package1 = f1.getDeclaringClass().getPackage();
      final Package package2 = f1.getDeclaringClass().getPackage();
      if (getAccessScope() == Scope.PACKAGE) {
        return package1.equals(package2);
      } else {
        return true;
      }
    }
    
    @Override
    public int hashCode() {
      return element.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      FieldModel other = (FieldModel) obj;
      return element.getName().equals(other.element.getName());
    }
  }  

  public ClassModel(Class<?> clazz) {
    methods = methodsModel(clazz);
    fields = fieldsModel(clazz);
  }

  private static LinkedHashMap<Method, MethodModel> methodsModel(Class<?> clazz) {
    return new ModelBuilder<Method, MethodModel>() {
      @Override
      protected Method [] members(Class<?> c) {
        Method[] declaredMethods = c.getDeclaredMethods();
        Arrays.sort(declaredMethods, methodSorter);
        return declaredMethods;
      }

      @Override
      protected MethodModel model(Method t) {
        return new MethodModel(t);
      }
    }.build(clazz);
  }

  private static LinkedHashMap<Field, FieldModel> fieldsModel(Class<?> clazz) {
    return new ModelBuilder<Field, FieldModel>() {
      @Override
      protected Field [] members(Class<?> c) {
        Field[] declaredFields = c.getDeclaredFields();
        Arrays.sort(declaredFields, fieldSorter);
        return declaredFields;
      }

      @Override
      protected FieldModel model(Field t) {
        return new FieldModel(t);
      }
    }.build(clazz);
  }

  public Map<Method,MethodModel> getMethods() {
    return Collections.unmodifiableMap(methods);
  }

  public Map<Field,FieldModel> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  public Map<Method,MethodModel> getAnnotatedLeafMethods(final Class<? extends Annotation> annotation) {
    LinkedHashMap<Method,MethodModel> result = new LinkedHashMap<Method,ClassModel.MethodModel>();
outer:
    for (Map.Entry<Method,MethodModel> e : getMethods().entrySet()) {
      MethodModel mm = e.getValue();

      if (mm.element.isAnnotationPresent(annotation)) {
        for (MethodModel next = mm.getDown(); next != null; next = next.getDown()) {
          if (next.element.isAnnotationPresent(annotation)) {
            // At least one override has the annotation on it, so skip any super methods
            // because it'd double the test.
            continue outer;
          }
        }

        result.put(e.getKey(), mm);
      }
    }
    return Collections.unmodifiableMap(result);
  }

  public <T extends Annotation> T getAnnotation(Method method, Class<T> annClass, boolean inherited) {
    MethodModel methodModel = methods.get(method);
    if (methodModel == null) {
      throw new IllegalArgumentException("No model for method: " + methodModel);
    }

    if (inherited) {
      for (; methodModel != null; methodModel = methodModel.getUp()) {
        T annValue = methodModel.element.getAnnotation(annClass);
        if (annValue != null) {
          return annValue;
        }
      }
      return null;
    } else {
      return method.getAnnotation(annClass);
    }
  }

  public <T extends Annotation> boolean isAnnotationPresent(Method method, Class<T> annClass, boolean inherited) {
    return getAnnotation(method, annClass, inherited) != null;
  }
}
