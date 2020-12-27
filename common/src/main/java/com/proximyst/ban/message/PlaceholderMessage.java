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

package com.proximyst.ban.message;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PlaceholderMessage implements IMessage {
  private final @NonNull MessageKey messageKey;
  private final @NonNull MessagesConfig messagesConfig;
  private final @NonNull IMessageComponent @NonNull [] messageComponents;
  private final @NonNull BanExceptionalFutureLogger<?> banExceptionalFutureLogger;

  @AssistedInject
  public PlaceholderMessage(final @Assisted @NonNull MessageKey messageKey,
      final @NonNull MessagesConfig messagesConfig,
      final @Assisted @NonNull IMessageComponent @NonNull [] messageComponents,
      final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory) {
    this.messageKey = messageKey;
    this.messagesConfig = messagesConfig;
    this.messageComponents = messageComponents;
    this.banExceptionalFutureLogger = banExceptionalFutureLoggerFactory.createLogger(this.getClass());
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> send(final @NonNull Audience audience,
      final @NonNull Identity source) {
    return this.component()
        .thenAccept(component -> audience.sendMessage(source, component))
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Component> component() {
    return this.awaitPlaceholders(this.messageComponents)
        .thenApply(placeholders -> MiniMessage.get().parse(this.messageKey.map(this.messagesConfig), placeholders))
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }

  private @NonNull CompletableFuture<@NonNull Map<@NonNull String, @NonNull String>> awaitPlaceholders(
      final @NonNull IMessageComponent @NonNull [] messageComponents) {
    if (messageComponents.length == 0) {
      return CompletableFuture.completedFuture(new HashMap<>());
    }

    final CompletableFuture<?>[] futures = Arrays.stream(messageComponents)
        .map(IMessageComponent::await)
        .toArray(CompletableFuture[]::new);

    return CompletableFuture.allOf(futures)
        .thenCompose($ -> this.recursePlaceholders(futures))
        .thenApply(map -> this.mergePlaceholders(futures, messageComponents, map));
  }

  private @NonNull CompletableFuture<@NonNull Map<@NonNull String, @NonNull String>> recursePlaceholders(
      final @NonNull CompletableFuture<@NonNull ?> @NonNull [] futures) {
    // What if we got an IMessageComponent([]) as value from `await`?
    // Well, we need to recursively merge those into our new map!
    return this.awaitPlaceholders(Arrays.stream(futures)
        .map(CompletableFuture::join)
        .filter(IMessageComponent[].class::isInstance)
        .flatMap(value -> Arrays.stream((IMessageComponent[]) value))
        .toArray(IMessageComponent[]::new));
  }

  private @NonNull Map<@NonNull String, @NonNull String> mergePlaceholders(
      final @NonNull CompletableFuture<@NonNull ?> @NonNull [] futures,
      final @NonNull IMessageComponent @NonNull [] components,
      final @NonNull Map<@NonNull String, @NonNull String> map) {
    for (int i = 0; i < components.length; ++i) {
      final IMessageComponent component = components[i];
      final Object value = futures[i].join();
      if (value instanceof IMessageComponent[]) {
        continue;
      }

      map.put(component.name(), String.valueOf(value));
    }

    return map;
  }
}
