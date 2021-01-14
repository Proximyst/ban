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

package com.proximyst.ban.event.subscriber;

import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.IUserService;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BannedPlayerJoinSubscriber {
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IUserService userService;
  private final @NonNull IMessageService messageService;

  @Inject
  BannedPlayerJoinSubscriber(final @NonNull IPunishmentService punishmentService,
      final @NonNull IUserService userService,
      final @NonNull IMessageService messageService) {
    this.punishmentService = punishmentService;
    this.userService = userService;
    this.messageService = messageService;
  }

  @Subscribe
  public void onJoinServer(final @NonNull LoginEvent event) {
    if (event.getPlayer().hasPermission(BanPermissions.BYPASS_BAN)) {
      // Don't bother to check players who can bypass bans.
      return;
    }

    final BanIdentity identity = this.userService.getUser(event.getPlayer().getUniqueId())
        .join() // They're currently online, so this should be completed instantly.
        .orElseThrow(() -> new IllegalStateException("online players must have identities"));
    this.punishmentService.getActiveBan(identity)
        .join() // This *should* be fast, and only on one player's connection thread
        .ifPresent(ban ->
            // TODO(Mariell Hoversholm)
            event.setResult(ComponentResult.denied(Component.text("banned"))));
  }
}
