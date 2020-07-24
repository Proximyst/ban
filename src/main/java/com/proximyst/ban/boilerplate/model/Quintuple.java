package com.proximyst.ban.boilerplate.model;

import java.util.Objects;

public class Quintuple<A, B, C, D, E> extends Quadruple<A, B, C, D> {
  private final E fifth;

  public Quintuple(A first, B second, C third, D fourth, E fifth) {
    super(first, second, third, fourth);
    this.fifth = fifth;
  }

  public E getFifth() {
    return fifth;
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
    Quintuple<?, ?, ?, ?, ?> quintuple = (Quintuple<?, ?, ?, ?, ?>) o;
    return Objects.equals(getFifth(), quintuple.getFifth());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getFifth());
  }
}
