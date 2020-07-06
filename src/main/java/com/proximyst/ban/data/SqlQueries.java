package com.proximyst.ban.data;

import com.proximyst.ban.utils.ResourceReader;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum SqlQueries {
  CREATE_TABLES("create_tables"),
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
}
