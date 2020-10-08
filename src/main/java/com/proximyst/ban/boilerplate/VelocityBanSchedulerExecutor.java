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

package com.proximyst.ban.boilerplate;

import com.proximyst.ban.BanPlugin;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link Executor} that just defers to the Velocity scheduler using the plugin instance.
 */
public final class VelocityBanSchedulerExecutor implements Executor {
  private final @NonNull BanPlugin main;

  public VelocityBanSchedulerExecutor(final @NonNull BanPlugin main) {
    this.main = main;
  }

  @Override
  public void execute(final @NonNull Runnable command) {
    this.main.getProxyServer().getScheduler().buildTask(this.main, command).schedule();
  }
}
