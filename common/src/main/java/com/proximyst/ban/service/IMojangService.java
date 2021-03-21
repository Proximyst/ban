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

package com.proximyst.ban.service;

import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A service to interact with the Mojang API, though not necessarily directly.
 */
public interface IMojangService {
  /**
   * Get the UUID of an identifier.
   *
   * @param identifier The identifier to get the UUID of.
   * @return The UUID of the identifier.
   */
  @NonNull CompletableFuture<@NonNull Optional<@NonNull UUID>> getUuid(final @NonNull String identifier);

  /**
   * Get the username of a UUID.
   *
   * @param uuid The UUID to get the username of.
   * @return The username of the UUID.
   */
  @NonNull CompletableFuture<@NonNull Optional<@NonNull String>> getUsername(final @NonNull UUID uuid);

  /**
   * Get a populated {@link UuidIdentity} for the user given.
   *
   * @param identifier Either the UUID of the user in string form (with or without hyphens), or their username.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUser(final @NonNull String identifier);

  /**
   * Get a populated {@link UuidIdentity} from the user given.
   *
   * @param uuid The UUID of the user.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUser(final @NonNull UUID uuid);
}
