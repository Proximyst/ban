package com.proximyst.ban.config;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public final class SqlConfig {
  @Setting(comment = "The address to connect to the SQL server at.")
  private String hostname = "localhost";

  @Setting(comment = "The port to connect to the SQL server at.")
  private short port = 3306;

  @Setting(comment = "The username for the SQL server.")
  private String username = "root";

  @Setting(comment = "The password to use for the SQL server.")
  private String password = "";

  @Setting(comment = "The database to use for the SQL server.")
  private String database = "ban";

  @Setting(comment = "The table prefix to use for the SQL tables.")
  private String tablePrefix = "ban_";

  @Setting(comment = "The max connections to have open in the pool.")
  private int maxConnections = 10;

  @NonNull
  public String getHostname() {
    return hostname;
  }

  public short getPort() {
    return port;
  }

  @NonNull
  public String getUsername() {
    return username;
  }

  @NonNull
  public String getPassword() {
    return password;
  }

  @NonNull
  public String getDatabase() {
    return database;
  }

  @NonNull
  public String getTablePrefix() {
    return tablePrefix;
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
    return getPort() == sqlConfig.getPort() &&
        getMaxConnections() == sqlConfig.getMaxConnections() &&
        getHostname().equals(sqlConfig.getHostname()) &&
        getUsername().equals(sqlConfig.getUsername()) &&
        getPassword().equals(sqlConfig.getPassword()) &&
        getDatabase().equals(sqlConfig.getDatabase()) &&
        getTablePrefix().equals(sqlConfig.getTablePrefix());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getHostname(),
        getPort(),
        getUsername(),
        getPassword(),
        getDatabase(),
        getTablePrefix(),
        getMaxConnections()
    );
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hostname", hostname)
        .add("port", port)
        .add("username", username)
        .add("password", password)
        .add("database", database)
        .add("tablePrefix", tablePrefix)
        .add("maxConnections", maxConnections)
        .toString();
  }
}
