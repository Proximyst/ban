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

package com.proximyst.ban.utils;

import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ArrayUtils {
  private ArrayUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @SafeVarargs
  public static <T> T @NonNull [] append(final T @NonNull [] array, final T @NonNull ... items) {
    final T[] newArray = Arrays.copyOf(array, array.length + items.length);
    System.arraycopy(items, 0, newArray, array.length, items.length);
    return newArray;
  }
}
