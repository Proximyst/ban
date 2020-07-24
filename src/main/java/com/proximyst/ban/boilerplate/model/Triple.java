package com.proximyst.ban.boilerplate.model;

import java.util.Objects;

public class Triple<A, B, C> extends Pair<A, B> {
  private final C third;

  public Triple(A first, B second, C third) {
    super(first, second);
    this.third = third;
  }

  public C getThird() {
    return third;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(getThird(), triple.getThird());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getThird());
  }
}
