package com.carrotsearch.randomizedtesting;

/**
 * A pair of things.
 */
final class Pair<A,B> {
  final A a;
  final B b;
  
  Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }
  
  @Override
  public int hashCode() {
    int hashA = (a == null ? 0 : a.hashCode());
    int hashB = (b == null ? 0 : b.hashCode());
    return hashA * 17 + hashB;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Pair<?,?>) {
      Pair<?, ?> other = (Pair<?, ?>) obj;
      return (a == null ? other.a == null : a.equals(other.a)) && 
             (b == null ? other.b == null : b.equals(other.b));
    }
    return false;
  }

  public static <T, E> Pair<T, E> newInstance(T t, E e) {
    return new Pair<T, E>(t, e);
  }
}
