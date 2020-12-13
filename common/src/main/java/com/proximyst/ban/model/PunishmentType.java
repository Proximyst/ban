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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
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
      .setId(0)
      .setCanLift(false)
      .setIsApplicable(false)
      .setNotificationPermission(BanPermissions.NOTIFY_WARN)
      .setBroadcastReasonless(MessageKey.BROADCAST_REASONLESS_WARN)
      .setBroadcastReasoned(MessageKey.BROADCAST_REASONED_WARN)
      .setVerbPastTense(MessageKey.FORMATTING_VERB_PAST_WARN)),

  /**
   * A mute on the player.
   * <p>
   * This means the player cannot communicate with other players.
   */
  MUTE(new PunishmentTypeBuilder()
      .setId(1)
      .setCanLift(true)
      .setIsApplicable(false)
      .setNotificationPermission(BanPermissions.NOTIFY_MUTE)
      .setBypassPermission(BanPermissions.BYPASS_MUTE)
      .setBroadcastReasonless(MessageKey.BROADCAST_REASONLESS_MUTE)
      .setBroadcastReasoned(MessageKey.BROADCAST_REASONED_MUTE)
      .setBroadcastLift(MessageKey.BROADCAST_UNMUTE)
      .setApplicationReasonless(MessageKey.APPLICATION_REASONLESS_MUTE)
      .setApplicationReasoned(MessageKey.APPLICATION_REASONED_MUTE)
      .setVerbPastTense(MessageKey.FORMATTING_VERB_PAST_MUTE)),

  /**
   * A kick on the player.
   * <p>
   * This means the player was forcefully disconnected once.
   */
  KICK(new PunishmentTypeBuilder()
      .setId(2)
      .setCanLift(false)
      .setIsApplicable(true)
      .setNotificationPermission(BanPermissions.NOTIFY_KICK)
      .setBypassPermission(BanPermissions.BYPASS_KICK)
      .setBroadcastReasonless(MessageKey.BROADCAST_REASONLESS_KICK)
      .setBroadcastReasoned(MessageKey.BROADCAST_REASONED_KICK)
      .setApplicationReasonless(MessageKey.APPLICATION_REASONLESS_KICK)
      .setApplicationReasoned(MessageKey.APPLICATION_REASONED_KICK)
      .setVerbPastTense(MessageKey.FORMATTING_VERB_PAST_KICK)),

  /**
   * A ban on the player.
   * <p>
   * This means the player was forcefully removed from the server for a period of time.
   */
  BAN(new PunishmentTypeBuilder()
      .setId(3)
      .setCanLift(true)
      .setIsApplicable(true)
      .setNotificationPermission(BanPermissions.NOTIFY_BAN)
      .setBypassPermission(BanPermissions.BYPASS_BAN)
      .setBroadcastReasonless(MessageKey.BROADCAST_REASONLESS_BAN)
      .setBroadcastReasoned(MessageKey.BROADCAST_REASONED_BAN)
      .setBroadcastLift(MessageKey.BROADCAST_UNBAN)
      .setApplicationReasonless(MessageKey.APPLICATION_REASONLESS_BAN)
      .setApplicationReasoned(MessageKey.APPLICATION_REASONED_BAN)
      .setVerbPastTense(MessageKey.FORMATTING_VERB_PAST_BAN)),

  /**
   * A note on the player.
   * <p>
   * This is not visible to the player in any way and is only used for the history.
   */
  NOTE(new PunishmentTypeBuilder()
      .setId(4)
      .setCanLift(false)
      .setIsApplicable(false)
      .setVerbPastTense(MessageKey.FORMATTING_VERB_PAST_NOTE)),
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

  /**
   * The permission required to bypass {@link Punishment}s of this type.
   */
  private final @Nullable String bypassPermission;

  private final @Nullable MessageKey broadcastReasonless;
  private final @Nullable MessageKey broadcastReasoned;
  private final @Nullable MessageKey broadcastLift;
  private final @Nullable MessageKey applicationReasonless;
  private final @Nullable MessageKey applicationReasoned;
  private final @NonNull MessageKey verbPastTense;

  PunishmentType(final @NonNull PunishmentTypeBuilder builder) {
    this.id = builder.id;
    this.canLift = builder.canLift;
    this.isApplicable = builder.isApplicable;
    this.notificationPermission = builder.notificationPermission;
    this.bypassPermission = builder.bypassPermission;
    this.broadcastReasonless = builder.broadcastReasonless;
    this.broadcastReasoned = builder.broadcastReasoned;
    this.broadcastLift = builder.broadcastLift;
    this.applicationReasonless = builder.applicationReasonless;
    this.applicationReasoned = builder.applicationReasoned;
    this.verbPastTense = builder.verbPastTense;
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
   * @return The permission to bypass a punishment of this type. This returns an {@link Optional#empty() empty Optional}
   * if it's not eligible for bypassing.
   */
  public @NonNull Optional<@NonNull String> getBypassPermission() {
    return Optional.ofNullable(this.bypassPermission);
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

  private static class PunishmentTypeBuilder {
    private byte id;
    private boolean canLift;
    private boolean isApplicable;
    private @Nullable String notificationPermission;
    private @Nullable String bypassPermission;
    private @Nullable MessageKey broadcastReasonless;
    private @Nullable MessageKey broadcastReasoned;
    private @Nullable MessageKey broadcastLift;
    private @Nullable MessageKey applicationReasonless;
    private @Nullable MessageKey applicationReasoned;
    private @MonotonicNonNull MessageKey verbPastTense;

    @Pure
    public @This PunishmentTypeBuilder setId(final int id) {
      this.id = (byte) id;
      return this;
    }

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

    @Pure
    public @This PunishmentTypeBuilder setBroadcastReasonless(final @Nullable MessageKey broadcastReasonless) {
      this.broadcastReasonless = broadcastReasonless;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setBroadcastReasoned(final @Nullable MessageKey broadcastReasoned) {
      this.broadcastReasoned = broadcastReasoned;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setBroadcastLift(final @Nullable MessageKey broadcastLift) {
      this.broadcastLift = broadcastLift;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setApplicationReasonless(final @Nullable MessageKey applicationReasonless) {
      this.applicationReasonless = applicationReasonless;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setApplicationReasoned(final @Nullable MessageKey applicationReasoned) {
      this.applicationReasoned = applicationReasoned;
      return this;
    }

    @Pure
    public @This PunishmentTypeBuilder setVerbPastTense(final @MonotonicNonNull MessageKey verbPastTense) {
      this.verbPastTense = verbPastTense;
      return this;
    }
  }
}
