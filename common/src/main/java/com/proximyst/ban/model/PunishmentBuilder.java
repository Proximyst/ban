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

// TODO(Proximyst): Google autovalues?
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

  /**
   * @param id The ID of the punishment. If no ID is set, use {@code -1}.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder id(final long id) {
    this.id = Math.max(id, -1);
    return this;
  }

  /**
   * @param punishmentType The {@link PunishmentType} of this punishment.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder type(final @NonNull PunishmentType punishmentType) {
    this.punishmentType = punishmentType;
    return this;
  }

  /**
   * @param target The target of this punishment.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder target(final @NonNull UUID target) {
    this.target = target;
    return this;
  }

  /**
   * @param punisher The punisher of this punishment.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder punisher(final @NonNull UUID punisher) {
    this.punisher = punisher;
    return this;
  }

  /**
   * @param reason The reason for this punishment.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder reason(final @Nullable String reason) {
    this.reason = reason;
    return this;
  }

  /**
   * @param lifted Whether this punishment is already lifted.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder lifted(final boolean lifted) {
    this.lifted = lifted;
    this.liftedBy = null;
    return this;
  }

  /**
   * @param liftedBy Who lifted the punishment.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder liftedBy(final @Nullable UUID liftedBy) {
    this.liftedBy = liftedBy;
    return this;
  }

  /**
   * @param time The time of the punishment.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder time(final long time) {
    this.time = time;
    return this;
  }

  /**
   * @param duration The duration of the punishment.
   * @return This {@link PunishmentBuilder} for chaining.
   */
  public @NonNull @This PunishmentBuilder duration(final long duration) {
    this.duration = duration;
    return this;
  }

  /**
   * @return A new {@link Punishment} derived from {@link PunishmentBuilder this builder}.
   */
  @RequiresNonNull({"target", "punisher", "punishmentType"})
  public @NonNull Punishment build() {
    return new Punishment(Math.max(this.id, -1),
        this.punishmentType,
        this.target,
        this.punisher,
        this.reason,
        this.lifted,
        this.liftedBy,
        this.time,
        this.duration);
  }
}
