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

import com.google.common.base.Preconditions;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.result.RowView;

/**
 * A punishment enacted on a player.
 */
public final class Punishment {
  /**
   * The type of this punishment.
   */
  private final @NonNull PunishmentType punishmentType;

  /**
   * The target of this punishment.
   * <p>
   * This is the punished player.
   */
  private final @NonNull UUID target;

  /**
   * The punisher of this punishment.
   * <p>
   * This is the one handing out and enforcing the punishment.
   */
  private final @NonNull UUID punisher;

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
   * The ID of the punishment in the database.
   */
  private long id;

  /**
   * The reason for the punishment if one is specified.
   * <p>
   * This must be a maximum of {@code 255} bytes long.
   */
  private @Nullable String reason;

  /**
   * Whether the punishment has been lifted.
   * <p>
   * May only be {@code true} if {@link PunishmentType#canBeLifted()} is {@code true}.
   */
  private boolean lifted;

  /**
   * By whom the punishment has been lifted if anyone.
   * <p>
   * This is {@code null} if the punishment has not been lifted or has simply expired.
   */
  private @Nullable UUID liftedBy;

  public Punishment(
      final @NonNull PunishmentType punishmentType,
      final @NonNull UUID target,
      final @NonNull UUID punisher,
      final @Nullable String reason,
      final boolean lifted,
      final @Nullable UUID liftedBy,
      final long time,
      final long duration
  ) {
    this(-1, punishmentType, target, punisher, reason, lifted, liftedBy, time, duration);
  }

  public Punishment(
      final long id,
      final @NonNull PunishmentType punishmentType,
      final @NonNull UUID target,
      final @NonNull UUID punisher,
      final @Nullable String reason,
      final boolean lifted,
      final @Nullable UUID liftedBy,
      final long time,
      final long duration
  ) {
    if (!lifted && liftedBy != null) {
      throw new IllegalArgumentException("liftedBy must be null if lifted is false");
    }

    this.id = Math.max(id, -1);
    this.punishmentType = Objects.requireNonNull(punishmentType, "type must be specified");
    this.target = Objects.requireNonNull(target, "target must be specified");
    this.punisher = Objects.requireNonNull(punisher, "punisher must be specified");
    this.reason = reason;
    this.lifted = lifted;
    this.liftedBy = liftedBy;
    this.time = time <= 0 ? System.currentTimeMillis() : time;
    this.duration = Math.max(duration, 0);
  }

  public static @NonNull Punishment fromRow(final @NonNull RowView row) {
    return new PunishmentBuilder()
        .id(row.getColumn("id", Long.class))
        .type(
            PunishmentType.getById(row.getColumn("type", Byte.class))
                .orElseThrow(() -> new IllegalStateException(
                    "punishment type id " + row.getColumn("type", Byte.class) + " is unknown"
                ))
        )
        .target(row.getColumn("target", UUID.class))
        .punisher(row.getColumn("punisher", UUID.class))
        .reason(row.getColumn("reason", String.class))
        .lifted(row.getColumn("lifted", Boolean.class))
        .liftedBy(row.getColumn("lifted_by", UUID.class))
        .time(row.getColumn("time", Long.class))
        .duration(row.getColumn("duration", Long.class))
        .build();
  }

  /**
   * @return The ID of this punishment, or an empty optional if none is known.
   */
  public @NonNull Optional<@NonNull @NonNegative Long> getId() {
    return this.id < 0 ? Optional.empty() : Optional.of(this.id);
  }

  /**
   * @param id Set the ID of this punishment.
   * @throws IllegalStateException If this punishment already has an ID.
   */
  public void setId(final long id) {
    if (this.getId().isPresent()) {
      throw new IllegalStateException("Cannot set ID of punishment with a pre-existing ID");
    }

    this.id = id;
  }

  /**
   * @return The type of this punishment.
   */
  public @NonNull PunishmentType getPunishmentType() {
    return this.punishmentType;
  }

  /**
   * @return The target of this punishment.
   * <p>
   * This is the punished player.
   */
  public @NonNull UUID getTarget() {
    return this.target;
  }

  /**
   * @return The punisher of this punishment.
   * <p>
   * This is the one handing out and enforcing the punishment.
   */
  public @NonNull UUID getPunisher() {
    return this.punisher;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public @NonNull Optional<@NonNull CommandSource> getPunisherAsSource(final @NonNull ProxyServer proxyServer) {
    if (this.getPunisher().equals(BanUser.CONSOLE.getUuid())) {
      return Optional.of(proxyServer.getConsoleCommandSource());
    }

    return (Optional) proxyServer.getPlayer(this.getPunisher());
  }

  /**
   * @return The reason for the punishment if one is specified, or {@code null} otherwise.
   * <p>
   * This is a maximum of {@code 255} bytes long.
   */
  public @NonNull Optional<@NonNull String> getReason() {
    return Optional.ofNullable(this.reason);
  }

  /**
   * @param reason The reason for the punishment or {@code null} otherwise. This must be a maximum of 255 bytes long.
   */
  public void setReason(final @Nullable String reason) {
    Preconditions.checkArgument(reason != null && reason.getBytes().length <= 255, "reason must be <= 255 bytes");
    this.reason = reason;
  }

  /**
   * @param reason The reason for the punishment or {@link Optional#empty()} otherwise. This must be a maximum of 255
   *               bytes long.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public void setReason(final @NonNull Optional<@NonNull String> reason) {
    reason.ifPresent(str -> Preconditions.checkArgument(str.getBytes().length <= 255, "reason must be <= 255 bytes"));
    this.reason = reason.orElse(null);
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
   * Sets the user who lifted the punishment.
   *
   * @param liftedBy The user who lifted this punishment.
   */
  public void setLiftedBy(final @NonNull UUID liftedBy) {
    this.lifted = true;
    this.liftedBy = liftedBy;
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

    if (this.getExpiration() > System.currentTimeMillis()) {
      // Expiration is in the future and not lifted, we'll have to wait.
      return true;
    }

    this.lifted = true;
    this.liftedBy = null; // Expired, no-one lifted it.
    return false;
  }
}
