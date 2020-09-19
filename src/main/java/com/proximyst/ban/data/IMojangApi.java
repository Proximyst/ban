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

package com.proximyst.ban.data;

import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.sewer.loadable.Loadable;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface IMojangApi {
  /**
   * Get the UUID of an identifier.
   *
   * @param identifier The identifier to get the UUID of.
   * @return The UUID of the identifier.
   */
  @NonNull Loadable<UUID> getUuid(@NonNull String identifier);

  /**
   * Get the username of a UUID.
   *
   * @param uuid The UUID to get the username of.
   * @return The username of the UUID.
   */
  @NonNull Loadable<String> getUsername(@NonNull UUID uuid);

  /**
   * Get the username history of a UUID.
   *
   * @param uuid The UUID to get the username history of.
   * @return The username history of the UUID, unsorted.
   */
  @NonNull Loadable<UsernameHistory> getUsernameHistory(@NonNull UUID uuid);

  /**
   * Get a populated {@link BanUser} for the user given.
   *
   * @param identifier Either the UUID of the user in string form (with or without hyphens), or their username.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull Loadable<BanUser> getUser(@NonNull String identifier);

  /**
   * Get a populated {@link BanUser} from the user given.
   *
   * @param uuid The UUID of the user.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull Loadable<BanUser> getUser(@NonNull UUID uuid);
}
