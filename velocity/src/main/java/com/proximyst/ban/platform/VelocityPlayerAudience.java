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

package com.proximyst.ban.platform;

import com.proximyst.ban.platform.IBanAudience.IBanPlayer;
import com.velocitypowered.api.proxy.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

public class VelocityPlayerAudience implements IBanPlayer, ForwardingAudience.Single {
  public static final @NonNull Map<UUID, VelocityPlayerAudience> AUDIENCE_CACHE = new HashMap<>();

  private final @NonNull Player player;

  private VelocityPlayerAudience(final @NonNull Player player) {
    this.player = player;
  }

  public static @NonNull VelocityPlayerAudience getAudience(final @NonNull Player source) {
    return AUDIENCE_CACHE.computeIfAbsent(source.getUniqueId(), $ -> new VelocityPlayerAudience(source));
  }

  @Pure
  public @NonNull Player player() {
    return this.player;
  }

  @Override
  @Pure
  public @NonNull UUID uuid() {
    return this.player.getUniqueId();
  }

  @Override
  @Pure
  public @NonNull String username() {
    return this.player.getUsername();
  }

  @Override
  public @NonNull Audience audience() {
    return this.player;
  }

  @Override
  public boolean hasPermission(final @NonNull String permission) {
    return this.player.hasPermission(permission);
  }

  @Override
  public void disconnect(final @NonNull Component reason) {
    this.player.disconnect(reason);
  }
}
