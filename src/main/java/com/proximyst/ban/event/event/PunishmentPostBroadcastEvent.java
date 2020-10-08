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
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PunishmentPostBroadcastEvent {
  private final @NonNull Punishment punishment;
  private final @NonNull Component message;

  public PunishmentPostBroadcastEvent(
      final @NonNull Punishment punishment,
      final @NonNull Component message
  ) {
    this.punishment = punishment;
    this.message = message;
  }

  public @NonNull Punishment getPunishment() {
    return this.punishment;
  }

  public @NonNull Component getMessage() {
    return this.message;
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PunishmentPostBroadcastEvent that = (PunishmentPostBroadcastEvent) o;
    return this.getPunishment().equals(that.getPunishment()) &&
        this.getMessage().equals(that.getMessage());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getPunishment(), this.getMessage());
  }
}
