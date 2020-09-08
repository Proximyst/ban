package com.proximyst.ban.model;

import static com.proximyst.ban.model.PunishmentType.NOTE;
import static com.proximyst.ban.model.PunishmentType.getById;

import com.google.common.base.Preconditions;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.boilerplate.model.Pair;
import com.proximyst.ban.boilerplate.model.Quadruple;
import com.proximyst.ban.boilerplate.model.Quintuple;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.event.event.PunishmentPostBroadcastEvent;
import com.proximyst.ban.event.event.PunishmentPreBroadcastEvent;
import com.proximyst.ban.event.event.PunishmentPreBroadcastParseEvent;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.time4j.ClockUnit;
import net.time4j.PrettyTime;
import net.time4j.format.TextWidth;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.result.RowView;

/**
 * A punishment enacted on a player.
 */
public final class Punishment {
  /**
   * The ID of the punishment in the database.
   */
  private long id;

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

  public Punishment(
      @NonNull PunishmentType punishmentType,
      @NonNull UUID target,
      @NonNull UUID punisher,
      @Nullable String reason,
      boolean lifted,
      @Nullable UUID liftedBy,
      long time,
      long duration
  ) {
    this(-1, punishmentType, target, punisher, reason, lifted, liftedBy, time, duration);
  }

  public Punishment(
      long id,
      @NonNull PunishmentType punishmentType,
      @NonNull UUID target,
      @NonNull UUID punisher,
      @Nullable String reason,
      boolean lifted,
      @Nullable UUID liftedBy,
      long time,
      long duration
  ) {
    if (!lifted && liftedBy != null) {
      throw new IllegalArgumentException("liftedBy must be null if lifted is false");
    }

    this.id = id < 0 ? -1 : id;
    this.punishmentType = Objects.requireNonNull(punishmentType, "type must be specified");
    this.target = Objects.requireNonNull(target, "target must be specified");
    this.punisher = Objects.requireNonNull(punisher, "punisher must be specified");
    this.reason = reason;
    this.lifted = lifted;
    this.liftedBy = liftedBy;
    this.time = time <= 0 ? System.currentTimeMillis() : time;
    this.duration = Math.max(duration, 0);
  }

  @NonNull
  public static Punishment fromRow(@NonNull RowView row) {
    return new PunishmentBuilder()
        .id(row.getColumn("id", Long.class))
        .type(
            getById(row.getColumn("type", Byte.class))
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
  @NonNull
  public Optional<@NonNegative Long> getId() {
    return id < 0 ? Optional.empty() : Optional.of(id);
  }

  /**
   * @param id Set the ID of this punishment.
   * @throws IllegalStateException If this punishment already has an ID.
   */
  public void setId(long id) {
    if (getId().isPresent()) {
      throw new IllegalStateException("Cannot set ID of punishment with a pre-existing ID");
    }

    this.id = id;
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @NonNull
  public Optional<CommandSource> getPunisherAsSource(@NonNull ProxyServer proxyServer) {
    if (getPunisher().equals(BanUser.CONSOLE.getUuid())) {
      return Optional.of(proxyServer.getConsoleCommandSource());
    }

    return (Optional) proxyServer.getPlayer(getPunisher());
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
   * @return Whether this punishment still applies to the player.
   */
  public boolean currentlyApplies(@NonNull BanPlugin main) {
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
    main.getSchedulerExecutor().execute(() -> {
      main.getLogger().info("Lifting punishment ID " + id + "; it has expired.");
      main.getDataInterface().liftPunishment(this);
    });
    return false;
  }

  /**
   * Broadcast the message to the entire proxy.
   *
   * @param main The main plugin instance.
   * @return Whether the broadcast was successful.
   */
  @SuppressWarnings("OptionalAssignedToNull") // That's the point, dumbo.
  @NonNull
  // TODO(Proximyst): Rewrite this... monstrosity...
  public CompletableFuture<@NonNull Boolean> broadcast(@NonNull BanPlugin main) {
    if (getPunishmentType() == NOTE) {
      // We do not broadcast notes.
      return CompletableFuture.completedFuture(true);
    }

    return main.getMojangApi().getUser(getTarget())
        .getOrLoad()
        .thenCombine(
            getPunisher().equals(BanUser.CONSOLE.getUuid())
                ? CompletableFuture.completedFuture(null)
                : main.getMojangApi().getUser(getPunisher()).getOrLoad(),
            (target, punisher) -> {
              String targetName = target
                  .map(BanUser::getUsername)
                  .orElseThrow(() -> new IllegalArgumentException("Target of punishment cannot be unknown"));
              String punisherName = punisher == null
                  ? CommandUtils.getSourceName(main.getProxyServer().getConsoleCommandSource())
                  : punisher
                      .map(BanUser::getUsername)
                      .orElseThrow(() -> new IllegalArgumentException("Punisher of punishment cannot be unknown"));

              // Prepare for aids...
              MessagesConfig cfg = main.getConfiguration().getMessages();
              String message = null;
              String permission = null;
              switch (getPunishmentType()) {
                case BAN:
                  message = getReason().isPresent() ? cfg.getBroadcastBanReason() : cfg.getBroadcastBanReasonless();
                  permission = BanPermissions.NOTIFY_BAN;
                  break;
                case KICK:
                  message = getReason().isPresent() ? cfg.getKickMessageReason() : cfg.getBroadcastKickReasonless();
                  permission = BanPermissions.NOTIFY_KICK;
                  break;
                case MUTE:
                  message = getReason().isPresent() ? cfg.getMuteMessageReason() : cfg.getMuteMessageReasonless();
                  permission = BanPermissions.NOTIFY_MUTE;
                  break;
                case WARNING:
                  message = getReason().isPresent() ? cfg.getBroadcastWarnReason() : cfg.getBroadcastWarnReasonless();
                  permission = BanPermissions.NOTIFY_WARN;
                  break;
              }
              if (message == null) {
                throw new IllegalStateException("No message was found for punishment: " + Punishment.this);
              }

              return new Quadruple<>(
                  targetName,
                  punisherName,
                  message,
                  permission
              );
            })
        .thenCompose(quad -> main.getProxyServer().getEventManager().fire(
            new PunishmentPreBroadcastParseEvent(this, quad.getThird())
        ).thenApply(e -> new Quintuple<>(quad.getFirst(), quad.getSecond(), quad.getThird(), quad.getFourth(), e)))
        .thenCompose(quin -> main.getProxyServer().getEventManager().fire(
            new PunishmentPreBroadcastEvent(
                this,

                MiniMessage.get()
                    .parse(
                        quin.getFifth().getMessageFormat(),

                        "name", quin.getFirst(),

                        "reason", getReason()
                            .map(MiniMessage.get()::escapeTokens)
                            .orElse("No reason specified"),

                        "duration", isPermanent()
                            ? main.getConfiguration().getMessages().getPermanently()
                            : main.getConfiguration().getMessages().getDurationFormat()
                                .replace("<duration>", PrettyTime.of(Locale.getDefault())
                                    .print(
                                        this.getExpiration() - System.currentTimeMillis(),
                                        ClockUnit.MILLIS,
                                        TextWidth.SHORT
                                    )),

                        "punisher", quin.getSecond()
                    )
            )
        ).thenApply(e -> new Pair<>(e.getMessage(), quin.getFourth())))
        .thenApply(pair -> {
          main.getProxyServer().getConsoleCommandSource().sendMessage(pair.getFirst());
          for (Player player : main.getProxyServer().getAllPlayers()) {
            if (!player.hasPermission(pair.getSecond())) {
              continue;
            }

            player.sendMessage(pair.getFirst());
          }
          main.getProxyServer().getEventManager().fireAndForget(
              new PunishmentPostBroadcastEvent(this, pair.getFirst())
          );
          return true;
        });
  }
}
