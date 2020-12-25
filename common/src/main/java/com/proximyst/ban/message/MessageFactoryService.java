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
import com.google.inject.Singleton;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.factory.IMessageFactory;
import com.proximyst.ban.model.BanUser;
import org.checkerframework.checker.nullness.qual.NonNull;

// The existence of this class is because of this issue:
// <https://github.com/google/guice/issues/1345>.
@Singleton
public final class MessageFactoryService {
  private final @NonNull IMessageFactory messageFactory;

  @Inject
  public MessageFactoryService(final @NonNull IMessageFactory messageFactory) {
    this.messageFactory = messageFactory;
  }

  public @NonNull IMessage commandsFeedbackUnban(final @NonNull BanUser target) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_UNBAN,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()));
  }
}
