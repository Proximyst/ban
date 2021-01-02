//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
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
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Pure;
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

  /**
   * Cast this logger to another return type. This ensures there's only 1 instance necessary, while working for any
   * return type.
   *
   * @param <R> The type to pretend to return.
   * @return This very instance of {@link BanExceptionalFutureLogger}.
   */
  @SuppressWarnings("unchecked") // We do not actually ever use the type; it is irrelevant.
  @Pure
  public <R> @This @NonNull BanExceptionalFutureLogger<R> cast() {
    return (BanExceptionalFutureLogger<R>) this;
  }
}
