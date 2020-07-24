package com.proximyst.ban.event.event;

import com.proximyst.ban.model.Punishment;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentPreBroadcastEvent implements ResultedEvent<GenericResult> {
  @NonNull
  private GenericResult result = GenericResult.allowed();

  @NonNull
  private final Punishment punishment;

  @NonNull
  private Component message;

  public PunishmentPreBroadcastEvent(
      @NonNull Punishment punishment,
      @NonNull Component message
  ) {
    this.punishment = punishment;
    this.message = message;
  }

  @Override
  @NonNull
  public GenericResult getResult() {
    return result;
  }

  @Override
  public void setResult(@NonNull GenericResult result) {
    this.result = result;
  }

  @NonNull
  public Punishment getPunishment() {
    return punishment;
  }

  @NonNull
  public Component getMessage() {
    return message;
  }

  public void setMessage(@NonNull Component message) {
    this.message = message;
  }
}
