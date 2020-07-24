package com.proximyst.ban.event.event;

import com.proximyst.ban.model.Punishment;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentPreBroadcastParseEvent {
  @NonNull
  private final Punishment punishment;

  @NonNull
  private String messageFormat;

  public PunishmentPreBroadcastParseEvent(
      @NonNull Punishment punishment,
      @NonNull String messageFormat
  ) {
    this.punishment = punishment;
    this.messageFormat = messageFormat;
  }

  @NonNull
  public Punishment getPunishment() {
    return punishment;
  }

  @NonNull
  public String getMessageFormat() {
    return messageFormat;
  }

  public void setMessageFormat(@NonNull String messageFormat) {
    this.messageFormat = messageFormat;
  }
}
