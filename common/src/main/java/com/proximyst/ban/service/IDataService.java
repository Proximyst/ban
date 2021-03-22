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

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.BanIdentity.IpIdentity;
import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A service that may fetch data from the data storage backend.
 * <p>
 * None of the methods are ran off-thread, and will therefore require you to do your own threading. They may all also
 * throw exceptions such as {@link SQLException} and {@link IllegalArgumentException}.
 */
public interface IDataService {
  /**
   * @return The prefix for the SQL files on the classpath for this service.
   */
  @NonNull String getClassPathPrefix();

  /**
   * Get all current punishments for a {@link BanIdentity}.
   *
   * @param identity The target of the punishments.
   * @return The punishments of the target in a mutable list.
   */
  @NonNull List<@NonNull Punishment> getPunishmentsForTarget(final @NonNull BanIdentity identity);

  /**
   * Save a punishment in the database.
   *
   * @param punishmentBuilder The punishment to save.
   * @return The new punishment created.
   */
  @NonNull Punishment savePunishment(final @NonNull PunishmentBuilder punishmentBuilder);

  /**
   * Lift a punishment.
   *
   * @param punishment The punishment to lift.
   * @param liftedBy The entity who lifted the punishment.
   * @return The lifted {@link Punishment}.
   */
  @NonNull Punishment liftPunishment(final @NonNull Punishment punishment, final @Nullable UUID liftedBy);

  /**
   * Get a {@link BanIdentity} from the database.
   * <p>
   * This will not fetch the identity's data if they are not in the database.
   *
   * @param uuid The identity to get.
   * @return The identity found, if any.
   */
  @NonNull Optional<@NonNull BanIdentity> getUser(final @NonNull UUID uuid);

  /**
   * Get a {@link BanIdentity} from the database.
   * <p>
   * This will not fetch the identity's data if they are not in the database.
   *
   * @param username The identity to get.
   * @return The identity found, if any.
   */
  @NonNull Optional<@NonNull BanIdentity> getUser(final @NonNull String username);

  /**
   * Get a {@link BanIdentity} from the database.
   * <p>
   * This will not fetch the identity's data if they are not in the database.
   *
   * @param id The ID of the identity to get.
   * @return The identity found, if any.
   */
  @NonNull Optional<@NonNull BanIdentity> getUser(final long id);

  /**
   * Get {@link UuidIdentity}s from the database.
   *
   * @param address The IP address to fetch the users by.
   * @return A list of all found users under the IP address at any given time.
   */
  @NonNull ImmutableList<@NonNull UuidIdentity> getUsersByIp(final @NonNull InetAddress address);

  /**
   * Get {@link UuidIdentity}s from the database.
   *
   * @param address The IP address to fetch the users by.
   * @return A list of all found users under the IP address at any given time.
   */
  default @NonNull ImmutableList<@NonNull UuidIdentity> getUsersByIp(final BanIdentity.@NonNull IpIdentity address) {
    return this.getUsersByIp(address.address());
  }

  /**
   * Get the date at which the identity was last updated, if ever.
   *
   * @param id The identity ID to get the cache time of.
   * @return The cache time, if any.
   */
  @NonNull Optional<@NonNull Long> getUserCacheDate(final long id);

  /**
   * Get the date at which the identity was last updated, if ever.
   *
   * @param id The identity ID to get the cache time of.
   * @return The cache time, if any.
   */
  @NonNull Optional<@NonNull Long> getUserCacheDate(final @NonNull UUID uuid);

  @NonNull UuidIdentity createIdentity(final @NonNull UUID uuid, final @NonNull String username);

  @NonNull IpIdentity createIdentity(final @NonNull InetAddress address,
      final @NonNull UuidIdentity @NonNull ... identities);

  /**
   * Update the expirations of all existing punishments.
   */
  void updateExpirations();
}
