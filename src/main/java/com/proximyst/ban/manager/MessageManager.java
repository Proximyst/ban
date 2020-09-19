package com.proximyst.ban.manager;

import com.google.inject.Singleton;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentType;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class MessageManager {
  @NonNull
  private final BanPlugin main;

  @NonNull
  private final MessagesConfig cfg;

  public MessageManager(
      @NonNull BanPlugin main,
      @NonNull MessagesConfig cfg
  ) {
    this.main = main;
    this.cfg = cfg;
  }

  @NonNull
  public Optional<@NonNull String> getNotificationPermissionOf(@NonNull PunishmentType type) {
    switch (type) {
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

  @NonNull
  public CompletableFuture<@NonNull Optional<@NonNull Component>> banNotification(@NonNull Punishment punishment) {
    String message = null;
    switch (punishment.getPunishmentType()) {
      case BAN:
        message = punishment.getReason().isPresent() ? cfg.broadcasts.banReason : cfg.broadcasts.banReasonless;
        break;
      case KICK:
        message = punishment.getReason().isPresent() ? cfg.broadcasts.kickReason : cfg.broadcasts.kickReasonless;
        break;
      case MUTE:
        message = punishment.getReason().isPresent() ? cfg.broadcasts.muteReason : cfg.broadcasts.muteReasonless;
        break;
      case WARNING:
        message = punishment.getReason().isPresent() ? cfg.broadcasts.warnReason : cfg.broadcasts.warnReasonless;
        break;

      case NOTE:
        // Fall through
      default:
        return CompletableFuture.completedFuture(Optional.empty());
    }

    if (message == null) {
      return CompletableFuture.completedFuture(Optional.empty());
    }
    if (message.trim().isEmpty()) {
      return CompletableFuture.completedFuture(Optional.of(TextComponent.empty()));
    }

    return formatMessageWith(message, punishment)
        .thenApply(Optional::of);
  }

  @NonNull
  public CompletableFuture<@NonNull Component> formatMessageWith(
      @NonNull String message,
      @NonNull Punishment punishment
  ) {
    return main.getUserManager().getUser(punishment.getTarget())
        .thenApply(opt ->
            opt.orElseThrow(() -> new IllegalArgumentException("Target of punishment cannot be unknown."))
        )
        .thenCombine(
            main.getUserManager().getUser(punishment.getPunisher()),
            (target, $punisher) -> {
              BanUser punisher = $punisher.orElseThrow(
                  () -> new IllegalArgumentException("Punisher of punishment cannot be unknown.")
              );
              return MiniMessage.get()
                  .parse(
                      message,

                      "targetName", target.getUsername(),
                      "targetUuid", target.getUuid().toString(),

                      "punisherName", punisher.getUsername(),
                      "punisherUuid", punisher.getUuid().toString(),

                      "punishmentId", punishment.getId().orElse(-1L).toString(),
                      "punishmentDate", SimpleDateFormat.getDateInstance().format(punishment.getDate()),
                      "reason", punishment.getReason()
                          .map(MiniMessage.get()::escapeTokens)
                          .orElse("No reason specified"),

                      "duration", punishment.isPermanent()
                          ? cfg.formatting.permanently
                          : cfg.formatting.durationFormat
                              .replace("<duration>",
                                  DurationFormatUtils.formatDurationWords(
                                      punishment.getExpiration() - System.currentTimeMillis(),
                                      false,
                                      false
                                  ))
                  );
            }
        );
  }
}
