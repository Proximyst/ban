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
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PunishmentAddedEvent implements ResultedEvent<GenericResult> {
  private @NonNull GenericResult result = GenericResult.allowed();
  private @NonNull Punishment punishment;

  public PunishmentAddedEvent(final @NonNull Punishment punishment) {
    this.punishment = Objects.requireNonNull(punishment);
  }

  @Override
  public @NonNull GenericResult getResult() {
    return this.result;
  }

  @Override
  public void setResult(final @NonNull GenericResult result) {
    this.result = Objects.requireNonNull(result);
  }

  public @NonNull Punishment getPunishment() {
    return this.punishment;
  }

  public void setPunishment(final @NonNull Punishment punishment) {
    this.punishment = Objects.requireNonNull(punishment);
  }
}
