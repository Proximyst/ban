package com.proximyst.ban.data;

import com.proximyst.ban.utils.ResourceReader;
import com.proximyst.ban.utils.ThrowableUtils;
import java.sql.SQLException;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

// TODO: Support more SQL dialects
public enum SqlQueries {
  SELECT_VERSION("select-version"),
  CREATE_VERSION_TABLE("create-version-table"),
  UPDATE_VERSION("update-version"),
  SELECT_PUNISHMENTS_BY_TARGET("select-punishments-by-target"),
  CREATE_PUNISHMENT("create-punishment"),
  LIFT_PUNISHMENT("lift-punishment"),
  ;

  @NonNull
  private final String query;

  SqlQueries(@NonNull String name) {
    this.query = ResourceReader.readResource("sql/" + name + ".sql");
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
