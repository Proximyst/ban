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

package com.proximyst.ban.platform;

import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class VelocityServer implements BanServer, ForwardingAudience.Single {
  private final @NonNull ProxyServer proxyServer;

  @Inject
  public VelocityServer(final @NonNull ProxyServer proxyServer) {
    this.proxyServer = proxyServer;
  }

  public @NonNull ProxyServer getProxyServer() {
    return this.proxyServer;
  }

  @Override
  public @NonNull Audience audience() {
    return this.proxyServer;
  }

  @Override
  public @NonNull Iterable<? extends BanAudience> connectedAudiences() {
    return (Iterable<BanAudience>) () -> Iterators.transform(this.proxyServer.getAllPlayers().iterator(),
        pl -> VelocityAudience.getAudience(Objects.requireNonNull(pl)));
  }
}
