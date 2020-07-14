package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public final class MessageConfig {
  @Setting(comment = "A command was incorrectly entered.")
  private String usage = "<red>Usage: <usage>";

  @NonNull
  public String getUsage() {
    return usage;
  }
}
