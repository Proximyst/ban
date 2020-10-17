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

package com.proximyst.ban.model;

import java.util.Collections;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BanUser {
  /**
   * The UUID used for the console in data storage.
   */
  private static final @NonNull UUID CONSOLE_UUID = new UUID(0, 0);

  /**
   * The user for a console.
   * <p>
   * Its {@link UUID} is {@code null} with its {@link UUID#getMostSignificantBits() most significant bits} and {@link
   * UUID#getLeastSignificantBits() least significant bits} being {@code 0}.
   */
  public static final @NonNull BanUser CONSOLE = new BanUser(
      CONSOLE_UUID,
      "CONSOLE",
      new UsernameHistory(CONSOLE_UUID, Collections.emptyList())
  );

  private final @NonNull UUID uuid;
  private final @NonNull String username;
  private final @NonNull UsernameHistory usernameHistory;

  public BanUser(
      final @NonNull UUID uuid,
      final @NonNull String username,
      final @NonNull UsernameHistory usernameHistory
  ) {
    this.uuid = uuid;
    this.username = username;
    this.usernameHistory = usernameHistory;
  }

  /**
   * @return This user's {@link UUID}.
   */
  public @NonNull UUID getUuid() {
    return this.uuid;
  }

  /**
   * @return This user's username.
   */
  public @NonNull String getUsername() {
    return this.username;
  }

  /**
   * @return This user's {@link UsernameHistory}.
   */
  public @NonNull UsernameHistory getUsernameHistory() {
    return this.usernameHistory;
  }
}
