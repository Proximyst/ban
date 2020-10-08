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
import com.proximyst.ban.manager.UserManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CacheUpdatePlayerJoinSubscriber {
  private final @NonNull UserManager userManager;

  @Inject
  public CacheUpdatePlayerJoinSubscriber(
      final @NonNull UserManager userManager
  ) {
    this.userManager = userManager;
  }

  @Subscribe
  public void onJoinServer(final @NonNull LoginEvent event) {
    this.userManager.updateUser(event.getPlayer().getUniqueId());
  }
}
