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

import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.CommandSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

public class VelocityAudience implements BanAudience {
  public static final @NonNull Map<UUID, VelocityAudience> AUDIENCE_CACHE = new HashMap<>();

  private final @NonNull CommandSource commandSource;
  private final @NonNull UUID uuid;

  private VelocityAudience(final @NonNull CommandSource commandSource) {
    this.commandSource = commandSource;
    this.uuid = CommandUtils.getSourceUuid(this.commandSource);
  }

  public static @NonNull VelocityAudience getAudience(final @NonNull CommandSource source) {
    return AUDIENCE_CACHE.computeIfAbsent(CommandUtils.getSourceUuid(source), $ -> new VelocityAudience(source));
  }

  @Pure
  public @NonNull CommandSource getCommandSource() {
    return this.commandSource;
  }

  @Override
  @Pure
  public @NonNull UUID uuid() {
    return this.uuid;
  }

  @Override
  public @NonNull Audience audience() {
    return this.commandSource;
  }

  @Override
  public boolean hasPermission(final @NonNull String permission) {
    return this.commandSource.hasPermission(permission);
  }
}
