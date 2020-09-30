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

import org.checkerframework.checker.nullness.qual.NonNull;

public final class ThrowableUtils {
  private ThrowableUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  /**
   * Sneaky throw the throwable.
   *
   * @param throwable The throwable to rethrow.
   */
  @SuppressWarnings("RedundantTypeArguments")
  public static void sneakyThrow(@NonNull final Throwable throwable) {
    throw ThrowableUtils.<RuntimeException>superSneaky(throwable);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> T superSneaky(@NonNull final Throwable throwable) throws T {
    throw (T) throwable;
  }
}
