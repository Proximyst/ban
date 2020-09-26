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

package com.proximyst.ban.data.impl;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Singleton;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.boilerplate.model.MigrationIndexEntry;
import com.proximyst.ban.boilerplate.model.Pair;
import com.proximyst.ban.data.IDataInterface;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.model.UsernameHistory.Entry;
import com.proximyst.ban.utils.ResourceReader;
import com.proximyst.ban.utils.ThrowableUtils;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;

/**
 * A MySQL based interface to the data of this plugin.
 */
@Singleton
public class MySqlInterface implements IDataInterface {
  @NonNull
  private static final String PATH = "sql/mysql/";

  @NonNull
  private final Logger logger;

  @NonNull
  private final Jdbi jdbi;

  public MySqlInterface(@NonNull Logger logger, @NonNull Jdbi jdbi) {
    this.logger = logger;
    this.jdbi = jdbi;
  }

  @Override
  public void applyMigrations() {
    String migrationsIndexJson = ResourceReader.readResource(PATH + "migrations/migrations-index.json");
    List<MigrationIndexEntry> migrations = BanPlugin.COMPACT_GSON
        .fromJson(
            migrationsIndexJson,
            new TypeToken<List<MigrationIndexEntry>>() {
            }.getType()
        );

    jdbi.useTransaction(handle -> {
      // Ensure the table exists first.
      SqlQueries.CREATE_VERSION_TABLE.forEachQuery(handle::execute);

      int version = handle.createQuery(SqlQueries.SELECT_VERSION.getQuery())
          .mapTo(int.class)
          .findOne()
          .orElse(0);
      migrations.stream()
          .filter(mig -> mig.getVersion() > version)
          .sorted(Comparator.comparingInt(MigrationIndexEntry::getVersion))
          .forEach(mig -> {
            logger.info("Migrating database to version " + mig.getVersion() + "...");
            String queries = ResourceReader.readResource(PATH + "migrations/" + mig.getPath());
            for (String query : queries.split(";")) {
              if (query.trim().isEmpty()) {
                continue;
              }

              handle.execute(query);
            }
            handle.execute(SqlQueries.UPDATE_VERSION.getQuery(), mig.getVersion());
          });
    });
  }

  @Override
  @NonNull
  public List<Punishment> getPunishmentsForTarget(@NonNull UUID target) {
    return jdbi.withHandle(handle ->
        handle.createQuery(SqlQueries.SELECT_PUNISHMENTS_BY_TARGET.getQuery())
            .bind(0, target)
            .map(Punishment::fromRow)
            .stream()
            .sorted(Comparator.comparingLong(Punishment::getTime))
            .collect(Collectors.toCollection(ArrayList::new)) // toList has no mutability guarantee
    );
  }

  @Override
  public void addPunishment(@NonNull final Punishment punishment) {
    jdbi.useHandle(handle -> {
      handle.execute(
          SqlQueries.CREATE_PUNISHMENT.getQuery(),
          punishment.getId().orElse(null),
          punishment.getPunishmentType().getId(),
          punishment.getTarget(),
          punishment.getPunisher(),
          punishment.getReason(),
          punishment.isLifted(),
          punishment.getLiftedBy(),
          punishment.getTime(),
          punishment.getDuration()
      );
      if (punishment.getId().isPresent()) {
        // We don't need to set the ID.
        return;
      }

      punishment.setId(
          handle.createQuery("SELECT LAST_INSERT_ID()")
              .mapTo(long.class)
              .one()
      );
    });
  }

  @Override
  public void liftPunishment(@NonNull Punishment punishment) {
    // Stay boxed to avoid an allocation.
    Long id = punishment.getId()
        .orElseThrow(() -> new IllegalArgumentException("Punishment must be in DB before lifting"));
    jdbi.useHandle(handle -> {
      handle.execute(
          SqlQueries.LIFT_PUNISHMENT.getQuery(),
          punishment.isLifted(),
          punishment.getLiftedBy(),
          id
      );
    });
  }

  @Override
  @NonNull
  public Optional<@NonNull BanUser> getUser(@NonNull UUID uuid) {
    return jdbi.withHandle(handle -> {
      UsernameHistory history = new UsernameHistory(
          uuid,
          handle.createQuery(SqlQueries.SELECT_USERNAME_HISTORY_BY_UUID.getQuery())
              .bind(0, uuid)
              .map(UsernameHistory.Entry::fromRow)
              .list()
      );

      return handle.createQuery(SqlQueries.SELECT_USER_BY_UUID.getQuery())
          .bind(0, uuid)
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
  @NonNull
  public Optional<@NonNull BanUser> getUser(@NonNull String username) {
    return jdbi.withHandle(handle -> {
      Pair<UUID, String> user = handle.createQuery(SqlQueries.SELECT_USER_BY_USERNAME.getQuery())
          .bind(0, username)
          .setMaxRows(1)
          .map((RowView rowView) -> new Pair<>(
              rowView.getColumn("uuid", UUID.class),
              rowView.getColumn("username", String.class)
          ))
          .findOne()
          .orElse(null);
      if (user == null) {
        return Optional.empty();
      }

      UsernameHistory history = new UsernameHistory(
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
  @NonNull
  public Optional<@NonNull Long> getUserCacheDate(@NonNull UUID uuid) {
    return jdbi.withHandle(handle ->
        handle.createQuery(SqlQueries.SELECT_USER_BY_UUID.getQuery())
            .bind(0, uuid)
            .setMaxRows(1)
            .map((RowView rowView) -> rowView.getColumn("timestamp", Timestamp.class))
            .findOne()
            .map(Timestamp::getTime)
    );
  }

  @Override
  public void saveUser(@NonNull BanUser user) {
    if (user == BanUser.CONSOLE) {
      return;
    }

    jdbi.useTransaction(handle -> {
      handle.execute(
          SqlQueries.SAVE_USER.getQuery(),
          user.getUuid(),
          user.getUsername()
      );

      for (Entry entry : user.getUsernameHistory().getEntries()) {
        handle.execute(
            SqlQueries.SAVE_USER_NAME.getQuery(),
            user.getUuid(),
            entry.getUsername(),
            entry.getChangedAt().map(date -> new Timestamp(date.getTime()))
        );
      }

      handle.commit();
    });
  }

  public enum SqlQueries {
    SELECT_VERSION("select-version"),
    CREATE_VERSION_TABLE("create-version-table"),
    UPDATE_VERSION("update-version"),
    SELECT_PUNISHMENTS_BY_TARGET("select-punishments-by-target"),
    CREATE_PUNISHMENT("create-punishment"),
    LIFT_PUNISHMENT("lift-punishment"),
    SELECT_USER_BY_UUID("select-user-by-uuid"),
    SELECT_USER_BY_USERNAME("select-user-by-username"),
    SELECT_USERNAME_HISTORY_BY_UUID("select-username-history-by-uuid"),
    SAVE_USER("save-user"),
    SAVE_USER_NAME("save-user-name"),
    ;

    @NonNull
    private final String query;

    SqlQueries(@NonNull String name) {
      this.query = ResourceReader.readResource(PATH + name + ".sql");
    }

    /**
     * Get the SQL query this wraps.
     *
     * @return The query for this enumeration.
     */
    @NonNull
    public String getQuery() {
      return query;
    }

    /**
     * Get the SQL queries this wraps, split by {@code ;}.
     *
     * @return The queries for this enumeration.
     */
    @NonNull
    public String[] getQueries() {
      return getQuery().split(";");
    }

    /**
     * Apply a {@link SqlConsumer} to each query gotten from {@link #getQueries()}.
     *
     * @param consumer The {@link SqlConsumer} to apply to each query.
     */
    public void forEachQuery(SqlConsumer consumer) {
      for (String query : getQueries()) {
        if (query.trim().isEmpty()) {
          continue;
        }

        consumer.accept(query);
      }
    }

    @FunctionalInterface
    public interface SqlConsumer extends Consumer<String> {
      @Override
      default void accept(String s) {
        try {
          apply(s);
        } catch (SQLException ex) {
          ThrowableUtils.sneakyThrow(ex);
        }
      }

      void apply(String query) throws SQLException;
    }
  }
}
