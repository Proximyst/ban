package com.proximyst.ban.event.event;

import com.proximyst.ban.model.Punishment;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentPostBroadcastEvent {
  @NonNull
  private final Punishment punishment;

  @NonNull
  private final Component message;

  public PunishmentPostBroadcastEvent(
      @NonNull Punishment punishment,
      @NonNull Component message
  ) {
    this.punishment = punishment;
    this.message = message;
  }

  @NonNull
  public Punishment getPunishment() {
    return punishment;
  }

  @NonNull
  public Component getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PunishmentPostBroadcastEvent that = (PunishmentPostBroadcastEvent) o;
    return getPunishment().equals(that.getPunishment()) &&
        getMessage().equals(that.getMessage());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPunishment(), getMessage());
  }
}
