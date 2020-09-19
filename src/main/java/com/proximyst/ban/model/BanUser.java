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
  @NonNull
  private static final UUID CONSOLE_UUID = new UUID(0, 0);

  @NonNull
  public static final BanUser CONSOLE = new BanUser(
      CONSOLE_UUID,
      "CONSOLE",
      new UsernameHistory(CONSOLE_UUID, Collections.emptyList())
  );

  @NonNull
  private final UUID uuid;

  @NonNull
  private final String username;

  @NonNull
  private final UsernameHistory usernameHistory;

  public BanUser(@NonNull UUID uuid, @NonNull String username, @NonNull UsernameHistory usernameHistory) {
    this.uuid = uuid;
    this.username = username;
    this.usernameHistory = usernameHistory;
  }

  @NonNull
  public UUID getUuid() {
    return uuid;
  }

  @NonNull
  public String getUsername() {
    return username;
  }

  @NonNull
  public UsernameHistory getUsernameHistory() {
    return usernameHistory;
  }
}
