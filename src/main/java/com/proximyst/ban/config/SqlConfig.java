package com.proximyst.ban.config;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public final class SqlConfig {
  @Setting(comment = "The JDBC URI to connect to the SQL server with.")
  private String jdbcUri = "jdbc:mysql://localhost:3306/ban";

  @Setting(comment = "The username for the SQL server.")
  private String username = "root";

  @Setting(comment = "The password to use for the SQL server.")
  private String password = "";

  @Setting(comment = "The max connections to have open in the pool.")
  private int maxConnections = 10;

  @NonNull
  public String getJdbcUri() {
    return jdbcUri;
  }

  @NonNull
  public String getUsername() {
    return username;
  }

  @NonNull
  public String getPassword() {
    return password;
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SqlConfig sqlConfig = (SqlConfig) o;
    return getMaxConnections() == sqlConfig.getMaxConnections() &&
        getJdbcUri().equals(sqlConfig.getJdbcUri()) &&
        getUsername().equals(sqlConfig.getUsername()) &&
        getPassword().equals(sqlConfig.getPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getJdbcUri(),
        getUsername(),
        getPassword(),
        getMaxConnections()
    );
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("jdbcUri", jdbcUri)
        .add("username", username)
        .add("password", password)
        .add("maxConnections", maxConnections)
        .toString();
  }
}
