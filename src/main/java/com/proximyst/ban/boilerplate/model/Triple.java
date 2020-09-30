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
import org.checkerframework.checker.nullness.qual.Nullable;

public class Triple<A, B, C> extends Pair<A, B> {
  private final C third;

  public Triple(final A first, final B second, final C third) {
    super(first, second);
    this.third = third;
  }

  public C getThird() {
    return this.third;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(this.getThird(), triple.getThird());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.getThird());
  }
}
