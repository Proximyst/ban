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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.config.MessagesConfig;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class StaticMessage implements IMessage {
  private final @NonNull MessageKey messageKey;
  private final @NonNull MessagesConfig messagesConfig;

  @Inject
  public StaticMessage(final @Assisted @NonNull MessageKey messageKey,
      final @NonNull MessagesConfig messagesConfig) {
    this.messageKey = messageKey;
    this.messagesConfig = messagesConfig;
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> send(final @NonNull Audience audience,
      final @NonNull Identity source) {
    return this.component()
        .thenAccept(component -> audience.sendMessage(source, component));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Component> component() {
    return CompletableFuture.completedFuture(MiniMessage.get().parse(this.messageKey.map(this.messagesConfig)));
  }
}
