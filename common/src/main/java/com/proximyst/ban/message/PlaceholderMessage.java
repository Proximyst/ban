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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
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
  public @NonNull CompletableFuture<@Nullable Void> send(final @NonNull Audience audience) {
    final CompletableFuture<?>[] futures = new CompletableFuture<?>[this.messageComponents.length];
    for (int i = 0; i < this.messageComponents.length; ++i) {
      futures[i] = this.messageComponents[i].await();
    }

    return CompletableFuture.allOf(futures)
        .thenRun(() -> {
          // All of them are done. Let's construct a map of the placeholders.
          // A map is required because we need the ability to return multiple placeholders.
          final Map<String, String> map = new HashMap<>(futures.length);
          for (int i = 0; i < this.messageComponents.length; ++i) {
            final CompletableFuture<?> future = futures[i];
            final Object value = future.join();
            if (value instanceof Map
                && (value.getClass().getTypeParameters().length == 0
                || String.class.isAssignableFrom(value.getClass().getTypeParameters()[0].getGenericDeclaration()))) {
              //noinspection unchecked -- Checked in above `if` statement
              for (final Map.Entry<String, ?> entry : ((Map<String, ?>) value).entrySet()) {
                map.put(entry.getKey(), String.valueOf(entry.getValue()));
              }
            } else {
              map.put(this.messageComponents[i].name(), String.valueOf(value));
            }
          }

          audience.sendMessage(MiniMessage.get().parse(this.messageKey.map(this.messagesConfig), map));
        })
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }
}
