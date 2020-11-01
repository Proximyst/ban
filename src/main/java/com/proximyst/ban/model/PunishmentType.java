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
import com.proximyst.ban.config.MessageKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
  WARNING(0, false, false, BanPermissions.NOTIFY_WARN,
      MessageKey.BROADCAST_REASONLESS_WARN, MessageKey.BROADCAST_REASONED_WARN, null,
      null, null,
      MessageKey.FORMATTING_VERB_PAST_WARN),

  /**
   * A mute on the player.
   * <p>
   * This means the player cannot communicate with other players.
   */
  MUTE(1, true, false, BanPermissions.NOTIFY_MUTE,
      MessageKey.BROADCAST_REASONLESS_MUTE, MessageKey.BROADCAST_REASONED_MUTE, MessageKey.BROADCAST_UNMUTE,
      MessageKey.APPLICATION_REASONLESS_MUTE, MessageKey.APPLICATION_REASONED_MUTE,
      MessageKey.FORMATTING_VERB_PAST_MUTE),

  /**
   * A kick on the player.
   * <p>
   * This means the player was forcefully disconnected once.
   */
  KICK(2, false, true, BanPermissions.NOTIFY_KICK,
      MessageKey.BROADCAST_REASONLESS_KICK, MessageKey.BROADCAST_REASONED_KICK, null,
      MessageKey.APPLICATION_REASONLESS_KICK, MessageKey.APPLICATION_REASONED_KICK,
      MessageKey.FORMATTING_VERB_PAST_KICK),

  /**
   * A ban on the player.
   * <p>
   * This means the player was forcefully removed from the server for a period of time.
   */
  BAN(3, true, true, BanPermissions.NOTIFY_BAN,
      MessageKey.BROADCAST_REASONLESS_BAN, MessageKey.BROADCAST_REASONED_BAN, MessageKey.BROADCAST_UNBAN,
      MessageKey.APPLICATION_REASONLESS_BAN, MessageKey.APPLICATION_REASONED_BAN,
      MessageKey.FORMATTING_VERB_PAST_BAN),

  /**
   * A note on the player.
   * <p>
   * This is not visible to the player in any way and is only used for the history.
   */
  NOTE(4, false, false, null,
      null, null, null,
      null, null,
      MessageKey.FORMATTING_VERB_PAST_NOTE),
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
   * Whether this {@link PunishmentType} can be applied to online players once placed.
   */
  private final boolean isApplicable;

  /**
   * The permission required to be notified of new {@link Punishment}s of this type.
   */
  private final @Nullable String notificationPermission;

  private final @Nullable MessageKey broadcastReasonless;
  private final @Nullable MessageKey broadcastReasoned;
  private final @Nullable MessageKey broadcastLift;
  private final @Nullable MessageKey applicationReasonless;
  private final @Nullable MessageKey applicationReasoned;
  private final @NonNull MessageKey verbPastTense;

  PunishmentType(final int id, final boolean canLift, final boolean isApplicable,
      final @Nullable String notificationPermission,
      final @Nullable MessageKey broadcastReasonless, final @Nullable MessageKey broadcastReasoned,
      final @Nullable MessageKey broadcastLift,
      final @Nullable MessageKey applicationReasonless, final @Nullable MessageKey applicationReasoned,
      final @NonNull MessageKey verbPastTense) {
    this.id = (byte) id;
    this.canLift = canLift;
    this.isApplicable = isApplicable;
    this.notificationPermission = notificationPermission;
    this.broadcastReasonless = broadcastReasonless;
    this.broadcastReasoned = broadcastReasoned;
    this.broadcastLift = broadcastLift;
    this.applicationReasonless = applicationReasonless;
    this.applicationReasoned = applicationReasoned;
    this.verbPastTense = verbPastTense;
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
   * @param hasReason Whether the punishment has a reason.
   * @return The {@link MessageKey} for broadcasting a punishment of this type.
   */
  public @NonNull Optional<@NonNull MessageKey> getBroadcastMessage(final boolean hasReason) {
    return Optional.ofNullable(hasReason ? this.broadcastReasoned : this.broadcastReasonless);
  }

  /**
   * @return The {@link MessageKey} for broadcasting lifting a punishment of this type.
   */
  public @NonNull Optional<@NonNull MessageKey> getBroadcastLiftMessage() {
    return Optional.ofNullable(this.broadcastLift);
  }

  /**
   * @param hasReason Whether the punishment has a reason.
   * @return The {@link MessageKey} for applying a punishment of this type.
   */
  public @NonNull Optional<@NonNull MessageKey> getApplicationMessage(final boolean hasReason) {
    return Optional.ofNullable(hasReason ? this.applicationReasoned : this.applicationReasonless);
  }

  /**
   * @return The {@link MessageKey} for applying a punishment of this type in a verb of past tense form.
   */
  public @NonNull MessageKey getVerbPastTense() {
    return this.verbPastTense;
  }
}
