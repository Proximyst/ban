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

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A user on the server, represented as an {@link Audience} and {@link Identity}.
 */
public interface IBanAudience extends Identified, Identity, Audience {
  /**
   * The UUID of this user.
   *
   * @return The UUID of the user.
   */
  @Override
  @SideEffectFree
  @NonNull UUID uuid();

  /**
   * The username of this user.
   *
   * @return The username of this user; this is not a deterministic result.
   */
  @SideEffectFree
  @NonNull String username();

  /**
   * Checks whether this user has the permission. If the platform supports tristates, it will return {@code true} if
   * granted, else the default value.
   *
   * @param permission The permission to check.
   * @return Whether the user has the permission given.
   */
  boolean hasPermission(@NonNull final String permission);

  /**
   * Disconnect this user, if applicable. This will not throw any kind of exception if the user is a console or
   * offline.
   *
   * @param reason The reason for disconnecting the user.
   */
  void disconnect(final @NonNull Component reason);

  /**
   * Disconnect this user, if applicable. This will not throw any kind of exception if the user is a console or
   * offline.
   *
   * @param reason The reason for disconnecting the user.
   */
  default void disconnect(final @NonNull ComponentLike reason) {
    this.disconnect(reason.asComponent());
  }

  /**
   * Cast this audience to a platform specific audience.
   *
   * @param <A> The type of {@link IBanAudience} for this platform.
   * @return The platform-specific {@link IBanAudience}.
   */
  @SuppressWarnings("unchecked") // This is intentional.
  @Pure
  default <A extends IBanAudience> @NonNull @This A castAudience() {
    return (A) this;
  }

  @Override
  @Pure
  default @NonNull Identity identity() {
    return this;
  }

  interface IBanConsole extends IBanAudience {
    @NonNull String USERNAME = "CONSOLE";
    @NonNull UUID UUID = new UUID(0, 0);
  }

  interface IBanPlayer extends IBanAudience {
  }
}
