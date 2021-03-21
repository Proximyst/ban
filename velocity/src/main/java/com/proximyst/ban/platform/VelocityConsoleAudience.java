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

import com.proximyst.ban.platform.IBanAudience.IBanConsole;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class VelocityConsoleAudience implements IBanConsole, ForwardingAudience.Single {
  private final @NonNull Audience console;

  @Inject
  VelocityConsoleAudience(final @NonNull ProxyServer proxyServer) {
    this.console = proxyServer.getConsoleCommandSource();
  }

  @Override
  public @NonNull UUID uuid() {
    return UUID;
  }

  @Override
  public @NonNull String username() {
    return USERNAME;
  }

  @Override
  public boolean hasPermission(final @NonNull String permission) {
    return true;
  }

  @Override
  public void disconnect(final @NonNull Component reason) {
  }

  @Override
  public @NonNull Audience audience() {
    return this.console;
  }
}
