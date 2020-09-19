//
// ban - A punishment suite for Velocity.
// Copyright (C) 2020 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

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
