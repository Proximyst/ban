//
// ban - A punishment suite for Velocity.
// Copyright (C) 2020 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package com.proximyst.ban.event.event;

import com.proximyst.ban.model.Punishment;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.ResultedEvent.GenericResult;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentPreBroadcastEvent implements ResultedEvent<GenericResult> {
  @NonNull
  private final Punishment punishment;

  @NonNull
  private GenericResult result = GenericResult.allowed();

  @NonNull
  private Component message;

  public PunishmentPreBroadcastEvent(
      @NonNull final Punishment punishment,
      @NonNull final Component message
  ) {
    this.punishment = punishment;
    this.message = message;
  }

  @Override
  @NonNull
  public GenericResult getResult() {
    return this.result;
  }

  @Override
  public void setResult(@NonNull final GenericResult result) {
    this.result = result;
  }

  @NonNull
  public Punishment getPunishment() {
    return this.punishment;
  }

  @NonNull
  public Component getMessage() {
    return this.message;
  }

  public void setMessage(@NonNull final Component message) {
    this.message = message;
  }
}
