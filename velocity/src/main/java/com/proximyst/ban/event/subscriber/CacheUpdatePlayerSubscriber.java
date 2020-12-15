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
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.platform.VelocityAudience;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.IUserService;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CacheUpdatePlayerSubscriber {
  private final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory;
  private final @NonNull IUserService userService;
  private final @NonNull IPunishmentService punishmentService;

  @Inject
  public CacheUpdatePlayerSubscriber(
      final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull IUserService userService,
      final @NonNull IPunishmentService punishmentService) {
    this.banExceptionalFutureLoggerFactory = banExceptionalFutureLoggerFactory;
    this.userService = userService;
    this.punishmentService = punishmentService;
  }

  @Subscribe
  public void onJoinServer(final @NonNull LoginEvent event) {
    this.userService.getUserUpdated(event.getPlayer().getUniqueId())
        .exceptionally(this.banExceptionalFutureLoggerFactory.createLogger(this.getClass()));
    this.punishmentService.getPunishments(event.getPlayer().getUniqueId()) // Get the punishments of the user.
        .exceptionally(this.banExceptionalFutureLoggerFactory.createLogger(this.getClass()));
  }

  @Subscribe(order = PostOrder.LAST)
  public void onJoinServerUpdateAudience(final @NonNull LoginEvent event) {
    if (event.getResult().isAllowed()) {
      // Cache the audience.
      VelocityAudience.getAudience(event.getPlayer());
    }
  }

  @Subscribe
  public void onLeaveServerUpdateAudience(final @NonNull DisconnectEvent event) {
    // We don't care how far along they were; they're gone.
    VelocityAudience.AUDIENCE_CACHE.remove(event.getPlayer().getUniqueId());
  }
}
