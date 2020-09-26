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
import com.proximyst.ban.model.Punishment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An interface to the data storage.
 */
public interface IDataInterface {
  /**
   * Apply all migrations to the database.
   */
  void applyMigrations();

  /**
   * Get all current punishments where the given {@link UUID} is the target.
   *
   * @param target The target of the punishments.
   * @return The punishments of the target in a mutable list.
   */
  @NonNull
  List<Punishment> getPunishmentsForTarget(@NonNull UUID target);

  /**
   * Add a punishment to the database.
   *
   * @param punishment The punishment to add.
   */
  void addPunishment(@NonNull Punishment punishment);

  /**
   * Lift a punishment in the database.
   *
   * @param punishment The punishment to lift.
   */
  void liftPunishment(@NonNull Punishment punishment);

  @NonNull
  Optional<@NonNull BanUser> getUser(@NonNull UUID uuid);

  @NonNull
  Optional<@NonNull BanUser> getUser(@NonNull String username);

  @NonNull
  Optional<@NonNull Long> getUserCacheDate(@NonNull UUID uuid);

  void saveUser(@NonNull BanUser user);
}
