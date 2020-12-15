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

import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

public final class BanExceptionalFutureLogger<T> implements Function<Throwable, T> {
  private final @NonNull Logger logger;
  private final @NonNull String name;

  public BanExceptionalFutureLogger(final @NonNull Logger logger,
      final @NonNull String name) {
    this.logger = logger;
    this.name = name;
  }

  @Override
  public @NonNull T apply(final @NonNull Throwable throwable) {
    this.logger.warn("[{}] Future has returned exceptionally", this.name, throwable);
    ThrowableUtils.sneakyThrow(throwable);
    throw new RuntimeException();
  }
}
