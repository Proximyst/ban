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

import com.proximyst.ban.BanPermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

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
  WARNING(0, false, true, false),

  /**
   * A mute on the player.
   * <p>
   * This means the player cannot communicate with other players.
   */
  MUTE(1, true, false),

  /**
   * A kick on the player.
   * <p>
   * This means the player was forcefully disconnected once.
   */
  KICK(2, false, true, true),

  /**
   * A ban on the player.
   * <p>
   * This means the player was forcefully removed from the server for a period of time.
   */
  BAN(3, true, true),

  /**
   * A note on the player.
   * <p>
   * This is not visible to the player in any way and is only used for the history.
   */
  NOTE(4, false, false, false),
  ;

  /**
   * All {@link PunishmentType}s by their IDs.
   */
  private static final @NonNull Map<@NonNull Byte, @NonNull PunishmentType> PUNISHMENT_TYPES_BY_ID = new HashMap<>();

  static {
    for (final PunishmentType type : values()) {
      PUNISHMENT_TYPES_BY_ID.put(type.getId(), type);
    }
  }

  /**
   * The ID of this {@link PunishmentType} in the database.
   */
  private final byte id;

  /**
   * Whether this {@link PunishmentType} can be lifted.
   */
  private final boolean canLift;

  /**
   * Whether this {@link PunishmentType} can be announced.
   */
  private final boolean isAnnouncable;

  /**
   * Whether this {@link PunishmentType} can be applied to online players once placed.
   */
  private final boolean isApplicable;

  PunishmentType(final byte id, final boolean canLift, final boolean isAnnouncable, final boolean isApplicable) {
    this.id = id;
    this.canLift = canLift;
    this.isAnnouncable = isAnnouncable;
    this.isApplicable = isApplicable;
  }

  PunishmentType(final int id, final boolean isAnnouncable, final boolean isApplicable) {
    this(id, true, isAnnouncable, isApplicable);
  }

  PunishmentType(final int id, final boolean canLift, final boolean isAnnouncable, final boolean isApplicable) {
    this((byte) id, canLift, isAnnouncable, isApplicable);
  }

  /**
   * @param id The ID of the punishment type.
   * @return An {@link Optional} possibly containing a {@link PunishmentType}.
   */
  public static @NonNull Optional<@NonNull PunishmentType> getById(final byte id) {
    return Optional.ofNullable(PUNISHMENT_TYPES_BY_ID.get(id));
  }

  /**
   * @return The ID of this {@link PunishmentType} in the database.
   */
  public byte getId() {
    return this.id;
  }

  /**
   * @return Whether this {@link PunishmentType} can be lifted.
   */
  public boolean canBeLifted() {
    return this.canLift;
  }

  /**
   * @return Whether this {@link PunishmentType} can be announced.
   */
  public boolean isAnnouncable() {
    return this.isAnnouncable;
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
    switch (this) {
      case BAN:
        return Optional.of(BanPermissions.NOTIFY_BAN);
      case KICK:
        return Optional.of(BanPermissions.NOTIFY_KICK);
      case MUTE:
        return Optional.of(BanPermissions.NOTIFY_MUTE);
      case WARNING:
        return Optional.of(BanPermissions.NOTIFY_WARN);

      case NOTE:
        // Fall through
      default:
        return Optional.empty();
    }
  }
}
