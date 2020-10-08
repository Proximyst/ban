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

package com.proximyst.ban.model;

import java.util.UUID;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

public final class PunishmentBuilder {
  private long id = -1;
  private @MonotonicNonNull PunishmentType punishmentType;
  private @MonotonicNonNull UUID target;
  private @MonotonicNonNull UUID punisher;
  private @Nullable String reason = null;
  private boolean lifted = false;
  private @Nullable UUID liftedBy = null;
  private long time = System.currentTimeMillis();
  private long duration = 0;

  public @NonNull @This PunishmentBuilder id(final long id) {
    this.id = id;
    return this;
  }

  public @NonNull @This PunishmentBuilder type(final @NonNull PunishmentType punishmentType) {
    this.punishmentType = punishmentType;
    return this;
  }

  public @NonNull @This PunishmentBuilder target(final @NonNull UUID target) {
    this.target = target;
    return this;
  }

  public @NonNull @This PunishmentBuilder punisher(final @NonNull UUID punisher) {
    this.punisher = punisher;
    return this;
  }

  public @NonNull @This PunishmentBuilder reason(final @Nullable String reason) {
    this.reason = reason;
    return this;
  }

  public @NonNull @This PunishmentBuilder lifted(final boolean lifted) {
    this.lifted = lifted;
    return this;
  }

  public @NonNull @This PunishmentBuilder liftedBy(final @Nullable UUID liftedBy) {
    this.liftedBy = liftedBy;
    return this;
  }

  public @NonNull @This PunishmentBuilder time(final long time) {
    this.time = time;
    return this;
  }

  public @NonNull @This PunishmentBuilder duration(final long duration) {
    if (duration == 0L) {
      return this.duration(-1L);
    }

    this.duration = duration;
    return this;
  }

  @RequiresNonNull({"target", "punisher", "punishmentType"})
  public @NonNull Punishment build() {
    return new Punishment(
        this.id,
        this.punishmentType,
        this.target,
        this.punisher,
        this.reason,
        this.lifted,
        this.liftedBy,
        this.time,
        this.duration
    );
  }
}
