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

import com.proximyst.ban.BanPlugin;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link Executor} that just defers to the Velocity scheduler using the plugin instance.
 */
public final class VelocityBanSchedulerExecutor implements Executor {
  private final @NonNull BanPlugin main;
  private final @NonNull ProxyServer proxyServer;

  @Inject
  VelocityBanSchedulerExecutor(final @NonNull BanPlugin main,
      final @NonNull ProxyServer proxyServer) {
    this.main = main;
    this.proxyServer = proxyServer;
  }

  @Override
  public void execute(final @NonNull Runnable command) {
    this.proxyServer.getScheduler().buildTask(this.main, command).schedule();
  }
}
