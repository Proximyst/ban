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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PunishmentBuilder {
  private long id = -1;
  private PunishmentType punishmentType;
  private UUID target;
  private UUID punisher;
  private @Nullable String reason = null;
  private boolean lifted = false;
  private @Nullable UUID liftedBy = null;
  private long time = System.currentTimeMillis();
  private long duration = 0;

  public PunishmentBuilder id(long id) {
    this.id = id;
    return this;
  }

  public PunishmentBuilder type(@NonNull PunishmentType punishmentType) {
    this.punishmentType = punishmentType;
    return this;
  }

  public PunishmentBuilder target(@NonNull UUID target) {
    this.target = target;
    return this;
  }

  public PunishmentBuilder punisher(@NonNull UUID punisher) {
    this.punisher = punisher;
    return this;
  }

  public PunishmentBuilder reason(@Nullable String reason) {
    this.reason = reason;
    return this;
  }

  public PunishmentBuilder lifted(boolean lifted) {
    this.lifted = lifted;
    return this;
  }

  public PunishmentBuilder liftedBy(@Nullable UUID liftedBy) {
    this.liftedBy = liftedBy;
    return this;
  }

  public PunishmentBuilder time(long time) {
    this.time = time;
    return this;
  }

  public PunishmentBuilder duration(long duration) {
    if (duration == 0L) {
      return this.duration(-1L);
    }

    this.duration = duration;
    return this;
  }

  public Punishment build() {
    return new Punishment(id, punishmentType, target, punisher, reason, lifted, liftedBy, time, duration);
  }
}