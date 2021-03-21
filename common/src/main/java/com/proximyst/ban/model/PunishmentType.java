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

import com.proximyst.ban.BanPermissions;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A form of punishment against a player.
 * <p>
 * All punishment types are permanently on the history of a player. Punishments that do not have an impact on the player
 * after it has been issued are therefore not liftable.
 */
public enum PunishmentType {
  /**
   * A warning on the player.
   */
  WARNING(new PunishmentTypeBuilder()
      .setCanLift(false)
      .setIsApplicable(false)
      .setNotificationPermission(BanPermissions.NOTIFY_WARN)),

  /**
   * A mute on the player.
   * <p>
   * This means the player cannot communicate with other players.
   */
  MUTE(new PunishmentTypeBuilder()
      .setCanLift(true)
      .setIsApplicable(false)
      .setNotificationPermission(BanPermissions.NOTIFY_MUTE)
      .setBypassPermission(BanPermissions.BYPASS_MUTE)),

  /**
   * A kick on the player.
   * <p>
   * This means the player was forcefully disconnected once.
   */
  KICK(new PunishmentTypeBuilder()
      .setCanLift(false)
      .setIsApplicable(true)
      .setNotificationPermission(BanPermissions.NOTIFY_KICK)
      .setBypassPermission(BanPermissions.BYPASS_KICK)),

  /**
   * A ban on the player.
   * <p>
   * This means the player was forcefully removed from the server for a period of time.
   */
  BAN(new PunishmentTypeBuilder()
      .setCanLift(true)
      .setIsApplicable(true)
      .setNotificationPermission(BanPermissions.NOTIFY_BAN)
      .setBypassPermission(BanPermissions.BYPASS_BAN)),

  /**
   * A note on the player.
   * <p>
   * This is not visible to the player in any way and is only used for the history.
   */
  NOTE(new PunishmentTypeBuilder()
      .setCanLift(false)
      .setIsApplicable(false)),
  ;

  /**
   * Whether this {@link PunishmentType} can be lifted.
   */
  private final boolean canLift;

  /**
   * Whether this {@link PunishmentType} can be applied to online players once placed.
   */
  private final boolean isApplicable;

  /**
   * The permission required to be notified of new {@link Punishment}s of this type.
   */
  private final @Nullable String notificationPermission;

  /**
   * The permission required to bypass {@link Punishment}s of this type.
   */
  private final @Nullable String bypassPermission;

  PunishmentType(final @NonNull PunishmentTypeBuilder builder) {
    this.canLift = builder.canLift;
    this.isApplicable = builder.isApplicable;
    this.notificationPermission = builder.notificationPermission;
    this.bypassPermission = builder.bypassPermission;
  }

  /**
   * @return Whether this {@link PunishmentType} can be lifted.
   */
  public boolean canBeLifted() {
    return this.canLift;
  }

  /**
   * @return Whether this {@link PunishmentType} can be applied to online players once placed.
   */
  public boolean isApplicable() {
    return this.isApplicable;
  }

  /**
   * @return The permission to view the notification of a punishment of this type. This returns an {@link
   * Optional#empty() empty Optional} if it's not eligible for notifications.
   */
  public @NonNull Optional<@NonNull String> getNotificationPermission() {
    return Optional.ofNullable(this.notificationPermission);
  }

  /**
   * @return The permission to bypass a punishment of this type. This returns an {@link Optional#empty() empty Optional}
   * if it's not eligible for bypassing.
   */
  public @NonNull Optional<@NonNull String> getBypassPermission() {
    return Optional.ofNullable(this.bypassPermission);
  }

  private static class PunishmentTypeBuilder {
    private boolean canLift;
    private boolean isApplicable;
    private @Nullable String notificationPermission;
    private @Nullable String bypassPermission;

    @Pure
    public @This PunishmentTypeBuilder setCanLift(final boolean canLift) {
      this.canLift = canLift;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setIsApplicable(final boolean isApplicable) {
      this.isApplicable = isApplicable;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setNotificationPermission(final @Nullable String notificationPermission) {
      this.notificationPermission = notificationPermission;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setBypassPermission(final @Nullable String bypassPermission) {
      this.bypassPermission = bypassPermission;
      return this;
    }
  }
}
