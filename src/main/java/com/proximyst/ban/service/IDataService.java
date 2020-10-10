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

package com.proximyst.ban.service;

import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface IDataService {
  /**
   * Get all current punishments where the given {@link UUID} is the target.
   *
   * @param target The target of the punishments.
   * @return The punishments of the target in a mutable list.
   */
  @NonNull List<@NonNull Punishment> getPunishmentsForTarget(final @NonNull UUID target);

  /**
   * Save a punishment in the database.
   *
   * @param punishment The punishment to save.
   */
  void savePunishment(final @NonNull Punishment punishment);

  /**
   * Get a {@link BanUser} from the database.
   * <p>
   * This will not fetch the user's data if they are not in the database.
   *
   * @param uuid The user to get.
   * @return The user found, if any.
   */
  @NonNull Optional<@NonNull BanUser> getUser(final @NonNull UUID uuid);

  /**
   * Get a {@link BanUser} from the database.
   * <p>
   * This will not fetch the user's data if they are not in the database.
   *
   * @param username The user to get.
   * @return The user found, if any.
   */
  @NonNull Optional<@NonNull BanUser> getUser(final @NonNull String username);

  /**
   * Get the date at which the user was cached, if they are cached.
   *
   * @param uuid The user to get the cache time of.
   * @return The cache time, if any.
   */
  @NonNull Optional<@NonNull Long> getUserCacheDate(final @NonNull UUID uuid);

  /**
   * Update the database with the user information.
   *
   * @param user The user to update the database with.
   */
  void saveUser(final @NonNull BanUser user);
}
