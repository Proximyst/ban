package com.proximyst.ban.event.event;

import com.proximyst.ban.model.Punishment;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentAddedEvent implements ResultedEvent<GenericResult> {
  @NonNull
  private GenericResult result = GenericResult.allowed();

  @NonNull
  private Punishment punishment;

  public PunishmentAddedEvent(@NonNull Punishment punishment) {
    this.punishment = Objects.requireNonNull(punishment);
  }

  @Override
  public GenericResult getResult() {
    return result;
  }

  @Override
  public void setResult(@NonNull GenericResult result) {
    this.result = Objects.requireNonNull(result);
  }

  @NonNull
  public Punishment getPunishment() {
    return punishment;
  }

  public void setPunishment(@NonNull Punishment punishment) {
    this.punishment = Objects.requireNonNull(punishment);
  }
}
