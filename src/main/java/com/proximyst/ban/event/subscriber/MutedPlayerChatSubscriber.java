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

package com.proximyst.ban.event.subscriber;

import com.google.inject.Inject;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MutedPlayerChatSubscriber {
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IMessageService messageService;

  @Inject
  public MutedPlayerChatSubscriber(final @NonNull IPunishmentService punishmentService,
      final @NonNull IMessageService messageService) {
    this.punishmentService = punishmentService;
    this.messageService = messageService;
  }

  @Subscribe
  public void onChat(final @NonNull PlayerChatEvent event) {
    if (event.getPlayer().hasPermission(BanPermissions.BYPASS_MUTE)) {
      // Don't bother checking if they're muted.
      return;
    }

    this.punishmentService.getActiveMute(event.getPlayer().getUniqueId())
        .join() // This *should* be fast, and only on one player's connection thread
        .ifPresent(mute -> {
          event.setResult(ChatResult.denied());

          final MessageKey applicationMessage = mute.getPunishmentType()
              .getApplicationMessage(mute.getReason().isPresent())
              .orElse(null);
          if (applicationMessage == null) {
            return;
          }
          this.messageService.sendFormattedMessage(event.getPlayer(), event.getPlayer(), applicationMessage);
        });
  }
}
