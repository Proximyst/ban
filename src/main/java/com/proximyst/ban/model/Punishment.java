package com.proximyst.ban.model;

import co.aikar.idb.DbRow;
import com.google.common.base.Preconditions;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A punishment enacted on a player.
 */
public final class Punishment {
  /**
   * The UUID used for the console in data storage.
   */
  @NonNull
  public static UUID CONSOLE_UUID = new UUID(0, 0);

  /**
   * The type of this punishment.
   */
  @NonNull
  private final PunishmentType punishmentType;

  /**
   * The target of this punishment.
   * <p>
   * This is the punished player.
   */
  @NonNull
  private final UUID target;

  /**
   * The punisher of this punishment.
   * <p>
   * This is the one handing out and enforcing the punishment.
   */
  @NonNull
  private final UUID punisher;

  /**
   * The reason for the punishment if one is specified.
   * <p>
   * This must be a maximum of {@code 255} bytes long.
   */
  @Nullable
  private String reason;

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
  @Nullable
  private UUID liftedBy;

  /**
   * The time at which this punishment was created.
   * <p>
   * This is in milliseconds since UNIX epoch.
   */
  private final long time;

  /**
   * The duration of the punishment in milliseconds.
   * <p>
   * If this is {@code 0}, the punishment is permanent.
   */
  private final long duration;

  /**
   * Whether the punishment is a silent one.
   * <p>
   * If {@code true}, this is not shown to players in their own histories, but is to staff members.
   */
  private final boolean silent;

  public Punishment(
      @NonNull PunishmentType punishmentType,
      @NonNull UUID target,
      @NonNull UUID punisher,
      @Nullable String reason,
      boolean lifted,
      @Nullable UUID liftedBy,
      long time,
      long duration,
      boolean silent
  ) {
    if (!lifted && liftedBy == null) {
      throw new IllegalArgumentException("liftedBy must be null if lifted is false");
    }

    this.punishmentType = Objects.requireNonNull(punishmentType, "type must be specified");
    this.target = Objects.requireNonNull(target, "target must be specified");
    this.punisher = Objects.requireNonNull(punisher, "punisher must be specified");
    this.reason = reason;
    this.lifted = lifted;
    this.liftedBy = liftedBy;
    this.time = time <= 0 ? System.currentTimeMillis() : time;
    this.duration = Math.max(duration, 0);
    this.silent = silent;
  }

  @NonNull
  public static Punishment fromRow(@NonNull DbRow row) {
    return new PunishmentBuilder()
        .type(
            PunishmentType.getById(row.getInt("type").byteValue())
                .orElseThrow(() -> new IllegalStateException(
                    "punishment type id " + row.getInt("type") + " is unknown"
                ))
        )
        .target(UUID.fromString(row.getString("target")))
        .punisher(UUID.fromString(row.getString("punisher")))
        .reason(row.getString("reason", null))
        .lifted(row.getInt("lifted") != 0)
        .liftedBy(Optional.ofNullable(row.getString("lifted_by")).map(UUID::fromString).orElse(null))
        .time(row.getLong("time"))
        .duration(row.getLong("duration"))
        .silent(row.getInt("silent") != 0)
        .build();
  }

  /**
   * @return The type of this punishment.
   */
  @NonNull
  public PunishmentType getPunishmentType() {
    return punishmentType;
  }

  /**
   * @return The target of this punishment.
   * <p>
   * This is the punished player.
   */
  @NonNull
  public UUID getTarget() {
    return target;
  }

  /**
   * @return The punisher of this punishment.
   * <p>
   * This is the one handing out and enforcing the punishment.
   */
  @NonNull
  public UUID getPunisher() {
    return punisher;
  }

  /**
   * @return The reason for the punishment if one is specified, or {@code null} otherwise.
   * <p>
   * This is a maximum of {@code 255} bytes long.
   */
  @NonNull
  public Optional<String> getReason() {
    return Optional.ofNullable(reason);
  }

  /**
   * @param reason The reason for the punishment or {@code null} otherwise. This must be a maximum of 255 bytes long.
   */
  public void setReason(@Nullable String reason) {
    Preconditions.checkArgument(reason != null && reason.getBytes().length <= 255, "reason must be <= 255 bytes");
    this.reason = reason;
  }

  /**
   * @param reason The reason for the punishment or {@link Optional#empty()} otherwise. This must be a maximum of 255
   *               bytes long.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public void setReason(@NonNull Optional<String> reason) {
    reason.ifPresent(str -> Preconditions.checkArgument(str.getBytes().length <= 255, "reason must be <= 255 bytes"));
    this.reason = reason.orElse(null);
  }

  /**
   * @return The time at which this punishment was created in milliseconds since UNIX epoch.
   */
  public long getTime() {
    return time;
  }

  /**
   * @return The time at which this punishment was created.
   */
  @NonNull
  public Date getDate() {
    return new Date(getTime());
  }

  /**
   * @return The duration of the punishment in milliseconds.
   * <p>
   * If this is {@code 0}, the punishment is permanent.
   */
  public long getDuration() {
    return duration;
  }

  /**
   * @return Whether the punishment has been lifted.
   * <p>
   * May only be {@code true} if {@link PunishmentType#canBeLifted()} is {@code true}.
   */
  public boolean isLifted() {
    return lifted;
  }

  /**
   * @return By whom the punishment has been lifted if anyone.
   * <p>
   * This is an empty {@link Optional} if the punishment has not been lifted or has simply expired.
   */
  @NonNull
  public Optional<UUID> getLiftedBy() {
    return Optional.ofNullable(liftedBy);
  }

  /**
   * @return Whether this punishment is permanent.
   */
  public boolean isPermanent() {
    return getDuration() == 0;
  }

  /**
   * @return The expiration time of this punishment in milliseconds since the UNIX epoch, or {@code -1} if it {@link
   * #isPermanent() is permanent}.
   */
  public long getExpiration() {
    if (isPermanent()) {
      return -1;
    }

    return getTime() + getDuration();
  }

  /**
   * @return The expiration time of this punishment, or an empty {@link Optional} if it {@link #isPermanent() is
   * permanent}.
   */
  @NonNull
  public Optional<Date> getExpirationDate() {
    if (isPermanent()) {
      return Optional.empty();
    }
    return Optional.of(new Date(getExpiration()));
  }

  /**
   * @return Whether the punishment is a silent one.
   * <p>
   * If {@code true}, this is not shown to players in their own histories, but is to staff members.
   */
  public boolean isSilent() {
    return silent;
  }

  /**
   * @return Whether this punishment still applies to the player.
   */
  public boolean currentlyApplies() {
    if (!getPunishmentType().canBeLifted()) {
      // The punishment cannot be lifted and therefore cannot apply past the event.
      return false;
    }

    if (isLifted() || isPermanent()) {
      // Is lifted already or is permanent.
      // Will return false if lifted, or true if permanent & unlifted.
      return !isLifted();
    }

    if (getExpiration() > System.currentTimeMillis()) {
      // Expiration is in the future and not lifted, we'll have to wait.
      return true;
    }

    lifted = true;
    liftedBy = null; // Expired, no-one lifted it.
    return false;
  }
}
