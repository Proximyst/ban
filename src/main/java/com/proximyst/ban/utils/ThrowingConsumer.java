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

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Represents an operation that accepts a single input argument and returns no result, optionally throwing an {@link
 * Exception}. Unlike most other functional interfaces, {@link ThrowingConsumer} is expected to operate via
 * side-effects.
 *
 * @param <T> The type of the input to the operation.
 * @param <E> The thrown exception type.
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
  /**
   * Performs this operation on the given argument.
   *
   * @param input The input argument.
   */
  void accept(final T input) throws E;

  /**
   * Returns a composed {@link ThrowingConsumer} that performs, in sequence, this operation followed by the {@code
   * after} operation. If performing either operation throws an exception, it is relayed to the caller of the composed
   * operation.  If performing this operation throws an exception, the {@code after} operation will not be performed.
   *
   * @param after The operation to perform after this operation.
   * @return A composed {@link ThrowingConsumer} that performs in sequence this operation followed by the {@code after}
   * operation.
   * @throws NullPointerException If {@code after} is null.
   */
  @SideEffectFree
  default @NonNull ThrowingConsumer<T, E> andThen(final @NonNull ThrowingConsumer<? super T, ? extends E> after) {
    return (T t) -> {
      this.accept(t);
      after.accept(t);
    };
  }

  /**
   * Convert this {@link ThrowingConsumer} to a {@link Consumer}.
   * <p>
   * This will {@link ThrowableUtils#sneakyThrow(Throwable) sneaky throw} the thrown {@link Exception}.
   *
   * @return A {@link Consumer} calling this {@link #accept(Object)}.
   */
  @SideEffectFree
  default @NonNull Consumer<T> toConsumer() {
    return input -> {
      try {
        this.accept(input);
      } catch (final Exception ex) {
        ThrowableUtils.sneakyThrow(ex);
      }
    };
  }
}
