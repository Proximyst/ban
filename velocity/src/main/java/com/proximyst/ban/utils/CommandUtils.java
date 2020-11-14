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

package com.proximyst.ban.utils;

import com.proximyst.ban.model.BanUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandUtils {
  private CommandUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  public static @NonNull String getSourceName(final @NonNull CommandSource source) {
    return source instanceof Player ? ((Player) source).getUsername() : BanUser.CONSOLE.getUsername();
  }

  public static @NonNull UUID getSourceUuid(final @NonNull CommandSource source) {
    return source instanceof Player ? ((Player) source).getUniqueId() : BanUser.CONSOLE.getUuid();
  }
}
