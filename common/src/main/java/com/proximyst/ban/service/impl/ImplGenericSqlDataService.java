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

package com.proximyst.ban.service.impl;

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.config.SqlConfig;
import com.proximyst.ban.factory.IIdentityFactory;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.BanIdentity.ConsoleIdentity;
import com.proximyst.ban.model.BanIdentity.IpIdentity;
import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.sql.IdentityType;
import com.proximyst.ban.model.sql.IpAddressType;
import com.proximyst.ban.platform.IBanAudience.IBanConsole;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.utils.ResourceReader;
import com.proximyst.ban.utils.ThrowableUtils;
import com.proximyst.ban.utils.ThrowingConsumer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;

@Singleton
public final class ImplGenericSqlDataService implements IDataService {
  private final @NonNull ConsoleIdentity consoleIdentity;
  private final @NonNull IIdentityFactory identityFactory;

  private final @NonNull Jdbi jdbi;
  private final @NonNull String path;

  private final @NonNull Query queryCreatePunishment;
  private final @NonNull Query queryLiftPunishment;
  private final @NonNull Query querySaveIdentity;
  private final @NonNull Query querySaveIpAddress;
  private final @NonNull Query querySaveUser;
  private final @NonNull Query querySelectIdentityById;
  private final @NonNull Query querySelectIdentityByIp;
  private final @NonNull Query querySelectIdentityByUsername;
  private final @NonNull Query querySelectIdentityByUuid;
  private final @NonNull Query querySelectPunishmentsByTarget;
  private final @NonNull Query querySelectUserByUsername;
  private final @NonNull Query querySelectUserByUuid;
  private final @NonNull Query querySelectUsersByIp;
  private final @NonNull Query queryUpdateExpirations;

  @Inject
  ImplGenericSqlDataService(final @NonNull ConsoleIdentity consoleIdentity,
      final @NonNull IIdentityFactory identityFactory,
      final @NonNull Jdbi jdbi,
      final @NonNull SqlConfig sqlConfig) {
    this.consoleIdentity = consoleIdentity;
    this.identityFactory = identityFactory;

    this.jdbi = jdbi;
    this.path = "sql/";

    this.queryCreatePunishment = new Query("create-punishment.sql", this.path);
    this.queryLiftPunishment = new Query("lift-punishment.sql", this.path);
    this.querySaveIdentity = new Query("save-identity.sql", this.path);
    this.querySaveIpAddress = new Query("save-ip-address.sql", this.path);
    this.querySaveUser = new Query("save-user.sql", this.path);
    this.querySelectIdentityById = new Query("select-identity-by-id.sql", this.path);
    this.querySelectIdentityByIp = new Query("select-identity-by-ip.sql", this.path);
    this.querySelectIdentityByUsername = new Query("select-identity-by-username.sql", this.path);
    this.querySelectIdentityByUuid = new Query("select-identity-by-uuid.sql", this.path);
    this.querySelectPunishmentsByTarget = new Query("select-punishments-by-target.sql", this.path);
    this.querySelectUserByUsername = new Query("select-user-by-username.sql", this.path);
    this.querySelectUserByUuid = new Query("select-user-by-uuid.sql", this.path);
    this.querySelectUsersByIp = new Query("select-users-by-ip.sql", this.path);
    this.queryUpdateExpirations = new Query("update-expirations.sql", this.path);
  }

  @Override
  public @NonNull String getClassPathPrefix() {
    return this.path;
  }

  @Override
  public @NonNull List<@NonNull Punishment> getPunishmentsForTarget(final @NonNull BanIdentity identity) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery(this.querySelectPunishmentsByTarget.getQuery())
            .bind("target", identity.getId())
            .mapTo(Punishment.class)
            .stream()
            .sorted(Comparator.comparingLong(Punishment::getTime))
            .collect(Collectors.toCollection(ArrayList::new)) // toList has no mutability guarantee
    );
  }

  @Override
  public @NonNull Punishment savePunishment(final @NonNull PunishmentBuilder punishment) {
    return this.jdbi.inTransaction(handle -> {
      final long id = handle.createUpdate(this.queryCreatePunishment.getQuery())
          .bind("type", punishment.getType().getId())
          .bind("target", punishment.getTarget().getId())
          .bind("punisher", punishment.getPunisher().getId())
          .bind("reason", punishment.getReason())
          .bind("lifted", punishment.isLifted())
          .bind("lifted_by", punishment.getLiftedBy())
          .bind("time", punishment.getTime())
          .bind("duration", punishment.getDuration())
          .executeAndReturnGeneratedKeys("id")
          .map((RowView row) -> row.getColumn("id", Long.class))
          .one();

      return new Punishment(id,
          punishment.getType(),
          punishment.getTarget(),
          punishment.getPunisher(),
          punishment.getReason(),
          punishment.isLifted(),
          punishment.getLiftedBy(),
          punishment.getTime(),
          punishment.getDuration());
    });
  }

  @Override
  public void liftPunishment(final @NonNull Punishment punishment, final @Nullable UUID liftedBy) {
    this.jdbi.useTransaction(handle ->
        handle.createUpdate(this.queryLiftPunishment.getQuery())
            .bind("lifted", true)
            .bind("lifted_by", liftedBy)
            .bind("id", punishment.getId())
            .execute());
  }

  @Override
  public @NonNull Optional<@NonNull BanIdentity> getUser(final @NonNull UUID uuid) {
    if (IBanConsole.UUID.equals(uuid)) {
      return Optional.of(this.consoleIdentity);
    }

    return this.jdbi.withHandle(handle -> handle.createQuery(this.querySelectIdentityByUuid.getQuery())
        .bind("uuid", uuid)
        .setMaxRows(1)
        .mapTo(BanIdentity.class)
        .findOne());
  }

  @Override
  public @NonNull Optional<@NonNull BanIdentity> getUser(final @NonNull String username) {
    return this.jdbi.withHandle(handle -> handle.createQuery(this.querySelectIdentityByUsername.getQuery())
        .bind("username", username)
        .setMaxRows(1)
        .mapTo(BanIdentity.class)
        .findOne());
  }

  @Override
  public @NonNull Optional<@NonNull BanIdentity> getUser(final long id) {
    return this.jdbi.withHandle(handle -> handle.createQuery(this.querySelectIdentityById.getQuery())
        .bind("id", id)
        .setMaxRows(1)
        .mapTo(BanIdentity.class)
        .findOne());
  }

  @Override
  public @NonNull ImmutableList<@NonNull UuidIdentity> getUsersByIp(final @NonNull InetAddress address) {
    final byte[] bytes = address.getAddress();

    return this.jdbi.withHandle(handle -> handle.createQuery(this.querySelectUsersByIp.getQuery())
        .bind("address", bytes)
        .mapTo(BanIdentity.class)
        .reduce(ImmutableList.<UuidIdentity>builder(), (builder, identity) -> {
          identity.asUuidIdentity().ifPresent(builder::add);
          return builder;
        })
        .build());
  }

  @Override
  public @NonNull Optional<@NonNull Long> getUserCacheDate(final long id) {
    if (id == 0) {
      return Optional.empty();
    }

    return this.jdbi.withHandle(handle ->
        handle.createQuery(this.querySelectIdentityById.getQuery())
            .bind("id", id)
            .setMaxRows(1)
            .map((RowView rowView) -> rowView.getColumn("timestamp", Timestamp.class))
            .findOne()
            .map(Timestamp::getTime));
  }

  @Override
  public @NonNull Optional<@NonNull Long> getUserCacheDate(final @NonNull UUID uuid) {
    if (IBanConsole.UUID.equals(uuid)) {
      return Optional.empty();
    }

    return this.jdbi.withHandle(handle ->
        handle.createQuery(this.querySelectIdentityByUuid.getQuery())
            .bind("uuid", uuid)
            .setMaxRows(1)
            .map((RowView rowView) -> rowView.getColumn("timestamp", Timestamp.class))
            .findOne()
            .map(Timestamp::getTime));
  }

  @Override
  public @NonNull UuidIdentity createIdentity(final @NonNull UUID uuid, final @NonNull String username) {
    return this.jdbi.inTransaction(handle -> {
      final long id = handle.createUpdate(this.querySaveIdentity.getQuery())
          .bind("type", IdentityType.UUID.type())
          .bind("uuid", uuid)
          .bindNull("address", Types.BINARY)
          .executeAndReturnGeneratedKeys("id")
          .map(row -> row.getColumn("id", Long.class))
          .one();

      return this.identityFactory.uuid(id, uuid, username);
    });
  }

  @Override
  public @NonNull IpIdentity createIdentity(final @NonNull InetAddress address,
      final @NonNull UuidIdentity @NonNull ... identities) {
    final IpAddressType type = address instanceof Inet4Address ? IpAddressType.IPV4 : IpAddressType.IPV6;
    final byte[] bytes = address.getAddress();

    return this.jdbi.inTransaction(handle -> {
      final long id = handle.createUpdate(this.querySaveIdentity.getQuery())
          .bind("type", type.type())
          .bindNull("uuid", Types.CHAR)
          .bind("address", bytes)
          .executeAndReturnGeneratedKeys("id")
          .map(row -> row.getColumn("id", Long.class))
          .one();

      final IpIdentity identity = this.identityFactory.ip(id, address);

      for (final UuidIdentity uuidIdentity : identities) {
        handle.createUpdate(this.querySaveIpAddress.getQuery())
            .bind("type", type.type())
            .bind("address", bytes)
            .bind("uuid", uuidIdentity.uuid());
      }

      return identity;
    });
  }

  @Override
  public void updateExpirations() {
    this.jdbi.useTransaction(handle -> handle.createUpdate(this.queryUpdateExpirations.getQuery()));
  }

  private static final class Query {
    private final @NonNull String query;

    private Query(final @NonNull String fileName,
        final @NonNull String path) {
      this.query = ResourceReader.readResource(path + fileName);
    }

    /**
     * Get the SQL query this wraps.
     *
     * @return The query for this enumeration.
     */
    @NonNull String getQuery() {
      return this.query;
    }

    /**
     * Get the SQL queries this wraps, split by {@code ;}.
     *
     * @return The queries for this enumeration.
     */
    @NonNull String @NonNull [] getQueries() {
      return this.getQuery().split(";");
    }

    /**
     * Apply a {@link ThrowingConsumer} to each query gotten from {@link #getQueries()}.
     *
     * @param consumer The {@link ThrowingConsumer} to apply to each query.
     */
    void forEachQuery(final @NonNull ThrowingConsumer<@NonNull String, SQLException> consumer) {
      for (final String query : this.getQueries()) {
        if (query.trim().isEmpty()) {
          continue;
        }

        try {
          consumer.accept(query);
        } catch (final SQLException ex) {
          ThrowableUtils.sneakyThrow(ex);
        }
      }
    }
  }
}
