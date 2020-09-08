package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
@NonNull
public final class SqlConfig {
  @Setting(comment = "The JDBC URI to connect to the SQL server with.")
  public String jdbcUri = "jdbc:mysql://localhost:3306/ban";

  @Setting(comment = "The username for the SQL server.")
  public String username = "root";

  @Setting(comment = "The password to use for the SQL server.")
  public String password = "";

  @Setting(comment = "The max connections to have open in the pool.")
  public int maxConnections = 10;
}
