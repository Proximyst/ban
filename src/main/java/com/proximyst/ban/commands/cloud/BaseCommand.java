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

package com.proximyst.ban.commands.cloud;

import cloud.commandframework.velocity.VelocityCommandManager;
import com.proximyst.ban.BanPlugin;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class BaseCommand {
  private final @NonNull BanPlugin main;

  public BaseCommand(@NonNull final BanPlugin main) {
    this.main = main;
  }

  public abstract void register(final @NonNull VelocityCommandManager<@NonNull CommandSource> commandManager);

  @NonNull
  protected BanPlugin getMain() {
    return this.main;
  }
}