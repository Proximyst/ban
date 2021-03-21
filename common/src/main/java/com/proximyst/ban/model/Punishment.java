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

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A punishment enacted on a player.
 */
public final class Punishment {
  /**
   * The type of this punishment.
   */
  private final @NonNull PunishmentType punishmentType;

  /**
   * The ID of the punishment in the database.
   */
  private final long id;

  /**
   * The target of this punishment.
   */
  private final @NonNull BanIdentity target;

  /**
   * The punisher of this punishment.
   */
  private final @NonNull BanIdentity punisher;

  /**
   * The time at which this punishment was created.
   * <p>
   * This is in milliseconds since UNIX epoch.
   */
  private final @NonNegative long time;

  /**
   * The duration of the punishment in milliseconds.
   * <p>
   * If this is {@code 0}, the punishment is permanent.
   */
  private final @NonNegative long duration;

  /**
   * The reason for the punishment if one is specified.
   */
  private final @Nullable String reason;

  /**
   * Whether the punishment has been lifted.
   * <p>
   * May only be {@code true} if {@link PunishmentType#canBeLifted()} is {@code true}.
   */
  private final boolean lifted;

  /**
   * By whom the punishment has been lifted if anyone.
   * <p>
   * This is {@code null} if the punishment has not been lifted or has simply expired.
   */
  private final @Nullable UUID liftedBy;

  public Punishment(final long id,
      final @NonNull PunishmentType punishmentType,
      final @NonNull BanIdentity target,
      final @NonNull BanIdentity punisher,
      final @Nullable String reason,
      final boolean lifted,
      final @Nullable UUID liftedBy,
      final long time,
      final long duration) {
    this.id = id;
    this.punishmentType = punishmentType;
    this.target = target;
    this.punisher = punisher;
    this.reason = reason;
    this.lifted = lifted;
    this.liftedBy = liftedBy;
    this.time = time;
    this.duration = duration;
  }

  /**
   * @return The ID of this punishment, or an empty optional if none is known.
   */
  public long getId() {
    return this.id;
  }

  /**
   * @return The type of this punishment.
   */
  public @NonNull PunishmentType getPunishmentType() {
    return this.punishmentType;
  }

  /**
   * @return The target of this punishment.
   */
  public @NonNull BanIdentity getTarget() {
    return this.target;
  }

  /**
   * @return The punisher of this punishment.
   */
  public @NonNull BanIdentity getPunisher() {
    return this.punisher;
  }

  /**
   * @return The reason for the punishment if one is specified.
   */
  public @NonNull Optional<@NonNull String> getReason() {
    return Optional.ofNullable(this.reason);
  }

  /**
   * @return The time at which this punishment was created in milliseconds since UNIX epoch.
   */
  public @NonNegative long getTime() {
    return this.time;
  }

  /**
   * @return The time at which this punishment was created.
   */
  public @NonNull Date getDate() {
    return new Date(this.getTime());
  }

  /**
   * @return The duration of the punishment in milliseconds.
   * <p>
   * If this is {@code 0}, the punishment is permanent.
   */
  public @NonNegative long getDuration() {
    return this.duration;
  }

  /**
   * @return Whether the punishment has been lifted.
   * <p>
   * May only be {@code true} if {@link PunishmentType#canBeLifted()} is {@code true}.
   */
  public boolean isLifted() {
    return this.lifted;
  }

  /**
   * @return By whom the punishment has been lifted if anyone.
   * <p>
   * This is an empty {@link Optional} if the punishment has not been lifted or has simply expired.
   */
  public @NonNull Optional<@NonNull UUID> getLiftedBy() {
    return Optional.ofNullable(this.liftedBy);
  }

  /**
   * @return Whether this punishment is permanent.
   */
  public boolean isPermanent() {
    return this.getDuration() == 0;
  }

  /**
   * @return The expiration time of this punishment in milliseconds since the UNIX epoch, or {@code -1} if it {@link
   * #isPermanent() is permanent}.
   * @see #isPermanent()
   * @see #getExpirationDate()
   */
  public long getExpiration() {
    if (this.isPermanent()) {
      return -1;
    }

    return this.getTime() + this.getDuration();
  }

  /**
   * @return The expiration time of this punishment, or an empty {@link Optional} if it {@link #isPermanent() is
   * permanent}.
   * @see #isPermanent()
   */
  public @NonNull Optional<@NonNull Date> getExpirationDate() {
    if (this.isPermanent()) {
      return Optional.empty();
    }

    return Optional.of(new Date(this.getExpiration()));
  }

  /**
   * @return Whether this punishment still applies to the player.
   */
  public boolean currentlyApplies() {
    if (!this.getPunishmentType().canBeLifted()) {
      // The punishment cannot be lifted and therefore cannot apply past the event.
      return false;
    }

    if (this.isLifted() || this.isPermanent()) {
      // Is lifted already or is permanent.
      // Will return false if lifted, or true if permanent & unlifted.
      return !this.isLifted();
    }

    // Expiration is in the future and not lifted, we'll have to wait.
    return this.getExpiration() > System.currentTimeMillis();
  }
}
