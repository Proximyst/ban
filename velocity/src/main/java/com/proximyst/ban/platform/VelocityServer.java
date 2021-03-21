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

import com.google.common.collect.Iterators;
import com.proximyst.ban.platform.IBanAudience.IBanConsole;
import com.proximyst.ban.platform.IBanAudience.IBanPlayer;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public class VelocityServer implements IBanServer, ForwardingAudience.Single {
  private final @NonNull ProxyServer proxyServer;
  private final @NonNull VelocityConsoleAudience velocityConsoleAudience;

  @Inject
  VelocityServer(final @NonNull ProxyServer proxyServer,
      final @NonNull VelocityConsoleAudience velocityConsoleAudience) {
    this.proxyServer = proxyServer;
    this.velocityConsoleAudience = velocityConsoleAudience;
  }

  @Override
  public @NonNull Audience audience() {
    return this.proxyServer;
  }

  @Override
  public @NonNull Iterable<? extends IBanPlayer> onlineAudiences() {
    return () -> Iterators.transform(this.proxyServer.getAllPlayers().iterator(),
        pl -> VelocityPlayerAudience.getAudience(Objects.requireNonNull(pl)));
  }

  @Override
  public @NonNull IBanConsole consoleAudience() {
    return this.velocityConsoleAudience;
  }

  @Override
  public @NonNegative int onlineCount() {
    return this.proxyServer.getPlayerCount();
  }

  @Override
  public @Nullable IBanPlayer audienceOf(final @NonNull UUID uuid) {
    return this.proxyServer.getPlayer(uuid)
        .map(VelocityPlayerAudience::getAudience)
        .orElse(null);
  }

  @Override
  public @Nullable IBanPlayer audienceOf(final @NonNull String username) {
    return this.proxyServer.getPlayer(username)
        .map(VelocityPlayerAudience::getAudience)
        .orElse(null);
  }

  @Override
  public boolean isOnlineMode() {
    return this.proxyServer.getConfiguration().isOnlineMode();
  }
}
