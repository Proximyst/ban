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
  /**
   * Create a new {@link IMessageComponent} whose content is static.
   *
   * @param name  The name of the placeholder.
   * @param value The value of the placeholder.
   * @return A new {@link IMessageComponent} with the content specified.
   */
  @Named("MessageComponent")
  @NonNull IMessageComponent staticComponent(final @Assisted("name") @NonNull String name,
      final @Assisted("value") @Nullable String value);

  /**
   * Create a new {@link IMessageComponent} whose content is static.
   *
   * @param name   The name of the placeholder.
   * @param future The eventual value of the placeholder.
   * @return A new {@link IMessageComponent} with the content specified.
   */
  @Named("MessageComponent")
  @NonNull IMessageComponent awaitedComponent(final @Assisted @NonNull String name,
      final @Assisted @NonNull CompletableFuture<@Nullable String> future);

  /**
   * Create a new {@link IMessageComponent} whose content is based on a {@link MessageKey}.
   *
   * @param name       The name of the placeholder.
   * @param messageKey The value of the placeholder.
   * @return A new {@link IMessageComponent} with the content specified.
   */
  @Named("MessageKeyComponent")
  @NonNull IMessageComponent keyComponent(final @Assisted @NonNull String name,
      final @Assisted @NonNull MessageKey messageKey);

  /**
   * Create a new {@link IMessageComponent} whose content is solely more {@link IMessageComponent}s. This means all the
   * inner {@link IMessageComponent components} will be {@link IMessageComponent#await() awaited}, then replaced in the
   * final {@link IMessageComponent}.
   *
   * @param messageComponents The components this {@link IMessageComponent} shall consist of.
   * @return A new {@link IMessageComponent} with the specified {@link IMessageComponent}s' values.
   */
  @Named("MessageMessageComponentComponent")
  @NonNull IMessageComponent componentComponent(
      final @Assisted @NonNull IMessageComponent @NonNull [] messageComponents);

  /**
   * Create a new {@link IMessageComponent} whose content is solely more {@link IMessageComponent}s. This means all the
   * inner {@link IMessageComponent components} will be {@link IMessageComponent#await() awaited}, then replaced in the
   * final {@link IMessageComponent}.
   *
   * @param messageComponents The eventual components this {@link IMessageComponent} shall consist of.
   * @return A new {@link IMessageComponent} with the specified {@link IMessageComponent}s' values.
   */
  @Named("MessageMessageComponentComponent")
  @NonNull IMessageComponent componentComponent(
      final @Assisted @NonNull CompletableFuture<@NonNull IMessageComponent @NonNull []> messageComponents);

  /**
   * Create a new {@link IMessage} whose content is static based of the value of a {@link MessageKey}.
   *
   * @param messageKey The key to use to fetch the message from.
   * @return A new {@link IMessage} with the content specified.
   */
  @Named("StaticMessage")
  @NonNull IMessage staticMessage(final @Assisted @NonNull MessageKey messageKey);

  /**
   * Create a new {@link IMessage} whose content is static based of the value of a {@link MessageKey} and the
   * placeholders of the specified {@link IMessageComponent}s.
   *
   * @param messageKey        The key to use to fetch the message from.
   * @param messageComponents The components to use for placeholders.
   * @return A new {@link IMessage} with the content specified.
   */
  @Named("PlaceholderMessage")
  @NonNull IMessage placeholderMessage(final @Assisted @NonNull MessageKey messageKey,
      final @Assisted @NonNull IMessageComponent @NonNull ... messageComponents);
}
