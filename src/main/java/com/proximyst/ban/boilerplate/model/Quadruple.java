package com.proximyst.ban.boilerplate.model;

import java.util.Objects;

public class Quadruple<A, B, C, D> extends Triple<A, B, C> {
  private final D fourth;

  public Quadruple(A first, B second, C third, D fourth) {
    super(first, second, third);
    this.fourth = fourth;
  }

  public D getFourth() {
    return fourth;
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
    Quadruple<?, ?, ?, ?> quadruple = (Quadruple<?, ?, ?, ?>) o;
    return Objects.equals(getFourth(), quadruple.getFourth());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getFourth());
  }
}
