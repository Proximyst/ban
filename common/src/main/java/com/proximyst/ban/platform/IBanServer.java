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
import com.proximyst.ban.platform.IBanAudience.IBanPlayer;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A server wrapper for the platform. This is an {@link Audience} as it is possible to send messages to the entire
 * server.
 */
public interface IBanServer extends Audience {
  /**
   * @return All online {@link IBanPlayer}s.
   */
  @NonNull Iterable<? extends IBanPlayer> onlineAudiences();

  /**
   * @return The console audience. There will only ever be one console.
   */
  @Pure
  @NonNull IBanConsole consoleAudience();

  /**
   * @return The amount of audiences there are online.
   */
  @NonNegative int onlineCount();

  /**
   * Gets a cached or creates a new {@link IBanAudience} for the given user by their {@link UUID}, if they are online.
   *
   * @param uuid The UUID to get an audience of.
   * @return The audience of the user, if online.
   */
  @Nullable IBanPlayer audienceOf(final @NonNull UUID uuid);

  /**
   * Gets a cached or creates a new {@link IBanAudience} for the given user by their username, if they are online.
   *
   * @param username The username to get an audience of.
   * @return The audience of the user, if online.
   */
  @Nullable IBanPlayer audienceOf(final @NonNull String username);

  /**
   * @return Whether the server currently only accepts valid paying users of the game.
   */
  boolean isOnlineMode();
}
