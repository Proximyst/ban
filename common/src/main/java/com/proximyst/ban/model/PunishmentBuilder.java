//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
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

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

public final class PunishmentBuilder {
  private @MonotonicNonNull PunishmentType punishmentType;
  private @MonotonicNonNull BanIdentity target;
  private @MonotonicNonNull BanIdentity punisher;
  private long time = System.currentTimeMillis();
  private long duration = 0;
  private @Nullable String reason;
  private boolean lifted;
  private @Nullable UUID liftedBy;

  public @NonNull PunishmentType getType() {
    return Objects.requireNonNull(this.punishmentType, "punishment type must be set");
  }

  public @This @NonNull PunishmentBuilder type(final @NonNull PunishmentType type) {
    this.punishmentType = type;
    return this;
  }

  public @NonNull BanIdentity getTarget() {
    return Objects.requireNonNull(this.target, "target must be set");
  }

  public @This @NonNull PunishmentBuilder target(final @NonNull BanIdentity target) {
    this.target = target;
    return this;
  }

  public @NonNull BanIdentity getPunisher() {
    return Objects.requireNonNull(this.punisher, "punisher must be set");
  }

  public @This @NonNull PunishmentBuilder punisher(final @NonNull BanIdentity punisher) {
    this.punisher = punisher;
    return this;
  }

  public long getTime() {
    return this.time;
  }

  public @This @NonNull PunishmentBuilder time(final long time) {
    this.time = time <= 0 ? System.currentTimeMillis() : time;
    return this;
  }

  public long getDuration() {
    return this.duration;
  }

  public @This @NonNull PunishmentBuilder duration(final long duration) {
    this.duration = Math.max(duration, 0);
    return this;
  }

  public @This @NonNull PunishmentBuilder duration(final long duration, final @NonNull TimeUnit timeUnit) {
    this.duration = Math.max(timeUnit.toMillis(duration), 0);
    return this;
  }

  public @Nullable String getReason() {
    return this.reason;
  }

  public @This @NonNull PunishmentBuilder reason(final @Nullable String reason) {
    this.reason = reason;
    return this;
  }

  public boolean isLifted() {
    return this.lifted;
  }

  public @Nullable UUID getLiftedBy() {
    return this.liftedBy;
  }

  public @This @NonNull PunishmentBuilder lifted() {
    this.lifted = true;
    return this;
  }

  public @This @NonNull PunishmentBuilder lifted(final @Nullable UUID liftedBy) {
    this.lifted = true;
    this.liftedBy = liftedBy;
    return this;
  }

  public @This @NonNull PunishmentBuilder lifted(final boolean isLifted) {
    this.lifted = isLifted;
    return this;
  }

  public @This @NonNull PunishmentBuilder lifted(final boolean isLifted, final @Nullable UUID liftedBy) {
    this.lifted = isLifted;
    this.liftedBy = isLifted ? liftedBy : null;
    return this;
  }

  public @This @NonNull PunishmentBuilder unlifted() {
    this.lifted = false;
    return this;
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PunishmentBuilder that = (PunishmentBuilder) o;
    return this.time == that.time &&
        this.duration == that.duration &&
        this.lifted == that.lifted &&
        this.punishmentType == that.punishmentType &&
        Objects.equals(this.target, that.target) &&
        Objects.equals(this.punisher, that.punisher) &&
        Objects.equals(this.reason, that.reason) &&
        Objects.equals(this.liftedBy, that.liftedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.punishmentType,
        this.target,
        this.punisher,
        this.time,
        this.duration,
        this.reason,
        this.lifted,
        this.liftedBy);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("punishmentType", this.punishmentType)
        .append("target", this.target)
        .append("punisher", this.punisher)
        .append("time", this.time)
        .append("duration", this.duration)
        .append("reason", this.reason)
        .append("lifted", this.lifted)
        .append("liftedBy", this.liftedBy)
        .toString();
  }
}
