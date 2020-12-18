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

import cloud.commandframework.CommandManager;
import com.proximyst.ban.platform.IBanAudience;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A command, registered with Cloud's {@link CommandManager} and operates on a {@link IBanAudience} sender.
 */
public abstract class BaseCommand {
  /**
   * Register this command with the {@link CommandManager}.
   *
   * @param commandManager The manager to register commands with. This assumes the manager supports {@link IBanAudience}s
   *                       as a sender.
   */
  public abstract void register(final @NonNull CommandManager<@NonNull IBanAudience> commandManager);
}
