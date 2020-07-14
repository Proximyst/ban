package com.proximyst.ban.event.event;

import com.proximyst.ban.model.Punishment;
import com.velocitypowered.api.event.ResultedEvent;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentAddedEvent implements ResultedEvent<ResultedEvent.GenericResult> {
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

  @NonNull
  public Punishment getPunishment() {
    return punishment;
  }

  @Override
  public void setResult(@NonNull GenericResult result) {
    this.result = Objects.requireNonNull(result);
  }

  public void setPunishment(@NonNull Punishment punishment) {
    this.punishment = Objects.requireNonNull(punishment);
  }
}
