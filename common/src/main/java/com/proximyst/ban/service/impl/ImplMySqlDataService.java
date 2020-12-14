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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.Update;

@Singleton
public final class ImplMySqlDataService implements IDataService {
  private static final @NonNull String PATH = "sql/mysql/";

  private final @NonNull Jdbi jdbi;

  @Inject
  public ImplMySqlDataService(final @NonNull Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public @NonNull String getClassPathPrefix() {
    return PATH;
  }

  @Override
  public @NonNull List<@NonNull Punishment> getPunishmentsForTarget(final @NonNull UUID target) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery(SqlQueries.SELECT_PUNISHMENTS_BY_TARGET.getQuery())
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
      final Update update = handle.createUpdate(SqlQueries.CREATE_PUNISHMENT.getQuery())
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
          handle.createQuery(SqlQueries.SELECT_USERNAME_HISTORY_BY_UUID.getQuery())
              .bind("uuid", uuid)
              .map(UsernameHistory.Entry::fromRow)
              .list()
      );

      return handle.createQuery(SqlQueries.SELECT_USER_BY_UUID.getQuery())
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
      final Pair<UUID, String> user = handle.createQuery(SqlQueries.SELECT_USER_BY_USERNAME.getQuery())
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
          handle.createQuery(SqlQueries.SELECT_USERNAME_HISTORY_BY_UUID.getQuery())
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
        handle.createQuery(SqlQueries.SELECT_USER_BY_UUID.getQuery())
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
      handle.createUpdate(SqlQueries.SAVE_USER.getQuery())
          .bind("uuid", user.getUuid())
          .bind("username", user.getUsername())
          .execute();

      for (final Entry entry : user.getUsernameHistory().getEntries()) {
        handle.createUpdate(SqlQueries.SAVE_USER_NAME.getQuery())
            .bind("uuid", user.getUuid())
            .bind("username", entry.getUsername())
            .bind("timestamp", entry.getChangedAt().map(Date::getTime).map(Timestamp::new))
            .execute();
      }
    });
  }

  private enum SqlQueries {
    SELECT_PUNISHMENTS_BY_TARGET("select-punishments-by-target"),
    CREATE_PUNISHMENT("create-punishment"),
    SELECT_USER_BY_UUID("select-user-by-uuid"),
    SELECT_USER_BY_USERNAME("select-user-by-username"),
    SELECT_USERNAME_HISTORY_BY_UUID("select-username-history-by-uuid"),
    SAVE_USER("save-user"),
    SAVE_USER_NAME("save-user-name"),
    ;

    private final @NonNull String query;

    SqlQueries(final @NonNull String name) {
      this.query = ResourceReader.readResource(PATH + name + ".sql");
    }

    /**
     * Get the SQL query this wraps.
     *
     * @return The query for this enumeration.
     */
    public @NonNull String getQuery() {
      return this.query;
    }

    /**
     * Get the SQL queries this wraps, split by {@code ;}.
     *
     * @return The queries for this enumeration.
     */
    public @NonNull String @NonNull [] getQueries() {
      return this.getQuery().split(";");
    }

    /**
     * Apply a {@link ThrowingConsumer} to each query gotten from {@link #getQueries()}.
     *
     * @param consumer The {@link ThrowingConsumer} to apply to each query.
     */
    public void forEachQuery(final @NonNull ThrowingConsumer<@NonNull String, SQLException> consumer) {
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
