package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public final class MessageConfig {
  @Setting(comment = "A command was incorrectly entered.")
  private String usage = "<red>Usage: <usage>";

  @Setting(comment = "A player name was given but is unknown to the Mojang API.")
  private String unknownPlayer = "<red>Unknown player: <player>";

  @NonNull
  public String getUsage() {
    return usage;
  }

  @NonNull
  public String getUnknownPlayer() {
    return unknownPlayer;
  }
}
