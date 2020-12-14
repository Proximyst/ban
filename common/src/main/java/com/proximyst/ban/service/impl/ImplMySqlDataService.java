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

package com.proximyst.ban.service.impl;

import cloud.commandframework.types.tuples.Pair;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.config.SqlConfig;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.model.UsernameHistory.Entry;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.utils.ResourceReader;
import com.proximyst.ban.utils.ThrowableUtils;
import com.proximyst.ban.utils.ThrowingConsumer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.Update;

@Singleton
public final class ImplMySqlDataService implements IDataService {
  private final @NonNull Jdbi jdbi;
  private final @NonNull String path;

  private final @NonNull Query queryCreatePunishment;
  private final @NonNull Query querySaveUser;
  private final @NonNull Query querySaveUserName;
  private final @NonNull Query querySelectPunishmentsByTarget;
  private final @NonNull Query querySelectUserByUsername;
  private final @NonNull Query querySelectUserByUuid;
  private final @NonNull Query querySelectUsernameHistoryByUuid;

  @Inject
  public ImplMySqlDataService(final @NonNull Jdbi jdbi,
      final @NonNull SqlConfig sqlConfig) {
    this.jdbi = jdbi;
    this.path = "sql/" + SqlDialect.parse(sqlConfig.dialect) + "/";

    this.queryCreatePunishment = new Query("create-punishment.sql", this.path);
    this.querySaveUser = new Query("save-user.sql", this.path);
    this.querySaveUserName = new Query("save-user-name.sql", this.path);
    this.querySelectPunishmentsByTarget = new Query("select-punishments-by-target.sql", this.path);
    this.querySelectUserByUsername = new Query("select-user-by-username.sql", this.path);
    this.querySelectUserByUuid = new Query("select-user-by-uuid.sql", this.path);
    this.querySelectUsernameHistoryByUuid = new Query("select-username-history-by-uuid.sql", this.path);
  }

  @Override
  public @NonNull String getClassPathPrefix() {
    return this.path;
  }

  @Override
  public @NonNull List<@NonNull Punishment> getPunishmentsForTarget(final @NonNull UUID target) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery(this.querySelectPunishmentsByTarget.getQuery())
            .bind("target", target)
            .map(Punishment::fromRow)
            .stream()
            .sorted(Comparator.comparingLong(Punishment::getTime))
            .collect(Collectors.toCollection(ArrayList::new)) // toList has no mutability guarantee
    );
  }

  @Override
  public void savePunishment(final @NonNull Punishment punishment) {
    this.jdbi.useHandle(handle -> {
      final Update update = handle.createUpdate(this.queryCreatePunishment.getQuery())
          .bind("id", punishment.getId().orElse(null))
          .bind("type", punishment.getPunishmentType().getId())
          .bind("target", punishment.getTarget())
          .bind("punisher", punishment.getPunisher())
          .bind("lifted", punishment.isLifted())
          .bind("lifted_by", punishment.getLiftedBy())
          .bind("time", punishment.getTime())
          .bind("duration", punishment.getDuration());

      punishment.getId().ifPresentOrElse($ -> update.execute(),
          () -> punishment.setId(
              update.executeAndReturnGeneratedKeys("id")
                  .map((RowView row) -> row.getColumn("id", Long.class))
                  .one()));
    });
  }

  @Override
  public @NonNull Optional<@NonNull BanUser> getUser(final @NonNull UUID uuid) {
    return this.jdbi.withHandle(handle -> {
      final UsernameHistory history = new UsernameHistory(
          uuid,
          handle.createQuery(this.querySelectUsernameHistoryByUuid.getQuery())
              .bind("uuid", uuid)
              .map(UsernameHistory.Entry::fromRow)
              .list()
      );

      return handle.createQuery(this.querySelectUserByUuid.getQuery())
          .bind("uuid", uuid)
          .setMaxRows(1)
          .map((RowView rowView) -> new BanUser(
              uuid,
              rowView.getColumn("username", String.class),
              history
          ))
          .findOne();
    });
  }

  @Override
  public @NonNull Optional<@NonNull BanUser> getUser(final @NonNull String username) {
    return this.jdbi.withHandle(handle -> {
      final Pair<UUID, String> user = handle.createQuery(this.querySelectUserByUsername.getQuery())
          .bind("username", username)
          .setMaxRows(1)
          .map((RowView rowView) -> Pair.of(
              rowView.getColumn("uuid", UUID.class),
              rowView.getColumn("username", String.class)
          ))
          .findOne()
          .orElse(null);
      if (user == null) {
        return Optional.empty();
      }

      final UsernameHistory history = new UsernameHistory(
          user.getFirst(),
          handle.createQuery(this.querySelectUsernameHistoryByUuid.getQuery())
              .bind(0, user.getFirst())
              .map(UsernameHistory.Entry::fromRow)
              .list()
      );

      return Optional.of(new BanUser(
          user.getFirst(),
          user.getSecond(),
          history
      ));
    });
  }

  @Override
  public @NonNull Optional<@NonNull Long> getUserCacheDate(final @NonNull UUID uuid) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery(this.querySelectUserByUuid.getQuery())
            .bind(0, uuid)
            .setMaxRows(1)
            .map((RowView rowView) -> rowView.getColumn("timestamp", Timestamp.class))
            .findOne()
            .map(Timestamp::getTime)
    );
  }

  @Override
  public void saveUser(final @NonNull BanUser user) {
    if (user == BanUser.CONSOLE) {
      return;
    }

    this.jdbi.useTransaction(handle -> {
      handle.createUpdate(this.querySaveUser.getQuery())
          .bind("uuid", user.getUuid())
          .bind("username", user.getUsername())
          .execute();

      for (final Entry entry : user.getUsernameHistory().getEntries()) {
        handle.createUpdate(this.querySaveUserName.getQuery())
            .bind("uuid", user.getUuid())
            .bind("username", entry.getUsername())
            .bind("timestamp", entry.getChangedAt().map(Date::getTime).map(Timestamp::new))
            .execute();
      }
    });
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

  private enum SqlDialect {
    MYSQL("mysql"),
    MARIADB("mysql"),
    POSTGRESQL("postgresql"),
    ;

    private final @NonNull String path;

    SqlDialect(final @NonNull String path) {
      this.path = path;
    }

    @NonNull String getPath() {
      return this.path;
    }

    static @NonNull SqlDialect parse(final @NonNull String name) {
      switch (name.toLowerCase(Locale.ENGLISH)) {
        case "mysql":
        case "my":
        case "msql":
          return MYSQL;

        case "mariadb":
        case "maria":
        case "mdb":
          return MARIADB;

        case "postgresql":
        case "postgressql":
        case "pgsql":
        case "pg":
        case "psql":
        case "postgres":
        case "postgre":
        case "postgress":
          // Dear child has many names.
          return POSTGRESQL;

        default:
          throw new IllegalArgumentException(
              "Unknown SQL dialect: " + name + "; only `mysql`, `mariadb`, `postgresql` are valid");
      }
    }
  }
}
