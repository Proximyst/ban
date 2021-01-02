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

package com.proximyst.ban.message;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MessageMessageComponentComponent implements IMessageComponent {
  private final @NonNull CompletableFuture<@NonNull IMessageComponent @NonNull []> messageComponents;

  @AssistedInject
  public MessageMessageComponentComponent(
      final @Assisted @NonNull CompletableFuture<@NonNull IMessageComponent @NonNull []> messageComponents,
      final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory) {
    this.messageComponents = messageComponents
        .exceptionally(banExceptionalFutureLoggerFactory.createLogger(this.getClass()));
  }

  @AssistedInject
  public MessageMessageComponentComponent(final @Assisted @NonNull IMessageComponent @NonNull [] messageComponents,
      final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory) {
    this(CompletableFuture.completedFuture(messageComponents), banExceptionalFutureLoggerFactory);
  }

  @Override
  public @NonNull String name() {
    return ""; // We won't need the name here.
  }

  @Override
  public @NonNull CompletableFuture<@Nullable ?> await() {
    return this.messageComponents;
  }
}
