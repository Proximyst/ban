package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
@NonNull
public final class Configuration {
  @Setting
  public SqlConfig sql = new SqlConfig();

  @Setting
  public MessagesConfig messages = new MessagesConfig();
}
