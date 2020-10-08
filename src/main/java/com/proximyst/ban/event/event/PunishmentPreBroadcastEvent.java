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
  private final @NonNull Punishment punishment;
  private @NonNull GenericResult result = GenericResult.allowed();
  private @NonNull Component message;

  public PunishmentPreBroadcastEvent(
      final @NonNull Punishment punishment,
      final @NonNull Component message
  ) {
    this.punishment = punishment;
    this.message = message;
  }

  @Override
  public @NonNull GenericResult getResult() {
    return this.result;
  }

  @Override
  public void setResult(final @NonNull GenericResult result) {
    this.result = result;
  }

  public @NonNull Punishment getPunishment() {
    return this.punishment;
  }

  public @NonNull Component getMessage() {
    return this.message;
  }

  public void setMessage(final @NonNull Component message) {
    this.message = message;
  }
}
