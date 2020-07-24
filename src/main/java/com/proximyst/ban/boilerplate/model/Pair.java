package com.proximyst.ban.boilerplate.model;

import java.util.Objects;

public class Pair<A, B> {
  private final A first;
  private final B second;

  public Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public A getFirst() {
    return first;
  }

  public B getSecond() {
    return second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(getFirst(), pair.getFirst()) &&
        Objects.equals(getSecond(), pair.getSecond());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFirst(), getSecond());
  }
}
