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
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MessageKeyComponent implements IMessageComponent {
  private final @NonNull String name;
  private final @NonNull MessageKey messageKey;
  private final @NonNull MessagesConfig messagesConfig;

  @Inject
  public MessageKeyComponent(final @Assisted @NonNull String name,
      final @Assisted @NonNull MessageKey messageKey,
      final @NonNull MessagesConfig messagesConfig) {
    this.name = name;
    this.messageKey = messageKey;
    this.messagesConfig = messagesConfig;
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  @Override
  public @NonNull CompletableFuture<@NonNull ?> await() {
    return CompletableFuture.completedFuture(this.messageKey.map(this.messagesConfig));
  }
}
