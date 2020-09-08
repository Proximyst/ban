package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
@NonNull
public final class Configuration {
  @Setting(comment = "The SQL server settings.")
  private SqlConfig sql = new SqlConfig();

  @Setting
  private MessagesConfig messages = new MessagesConfig();

  @NonNull
  public SqlConfig getSql() {
    return sql;
  }

  @NonNull
  public MessagesConfig getMessages() {
    return messages;
  }
}
