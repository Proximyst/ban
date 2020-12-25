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

package com.proximyst.ban.factory;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.message.IMessage;
import com.proximyst.ban.message.IMessageComponent;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IMessageFactory {
  @Named("MessageComponent")
  @NonNull IMessageComponent staticComponent(final @Assisted("name") @NonNull String name,
      final @Assisted("value") @Nullable String value);

  @Named("MessageComponent")
  @NonNull IMessageComponent awaitedComponent(final @Assisted @NonNull String name,
      final @Assisted @NonNull CompletableFuture<@Nullable String> future);

  @Named("MessageKeyComponent")
  @NonNull IMessageComponent keyComponent(final @Assisted @NonNull String name,
      final @Assisted @NonNull MessageKey messageKey);

  @Named("MessageMessageComponentComponent")
  @NonNull IMessageComponent componentComponent(
      final @Assisted @NonNull IMessageComponent @NonNull [] messageComponents);

  @Named("MessageMessageComponentComponent")
  @NonNull IMessageComponent componentComponent(
      final @Assisted @NonNull CompletableFuture<@NonNull IMessageComponent @NonNull []> messageComponents);

  @Named("StaticMessage")
  @NonNull IMessage staticMessage(final @Assisted @NonNull MessageKey messageKey);

  @Named("PlaceholderMessage")
  @NonNull IMessage placeholderMessage(final @Assisted @NonNull MessageKey messageKey,
      final @Assisted @NonNull IMessageComponent @NonNull ... messageComponents);
}
