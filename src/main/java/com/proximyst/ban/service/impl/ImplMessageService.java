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

package com.proximyst.ban.service.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IUserService;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ImplMessageService implements IMessageService {
  private final @NonNull IUserService userService;
  private final @NonNull MessagesConfig cfg;
  private final @NonNull ProxyServer proxyServer;

  @Inject
  public ImplMessageService(final @NonNull IUserService userService,
      final @NonNull MessagesConfig messagesConfig,
      final @NonNull ProxyServer proxyServer) {
    this.userService = userService;
    this.cfg = messagesConfig;
    this.proxyServer = proxyServer;
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> announceNewPunishment(final @NonNull Punishment punishment) {
    final String message = punishment.getPunishmentType().getBroadcastMessage(
        punishment.getReason().isPresent()).map(key -> key.map(this.cfg)).orElse(null);
    if (message == null) {
      return CompletableFuture.completedFuture(null);
    }

    return this.announcePunishmentMessage(punishment, message);
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> announceLiftedPunishment(final @NonNull Punishment punishment) {
    final String message = punishment.getPunishmentType().getBroadcastLiftMessage()
        .map(key -> key.map(this.cfg)).orElse(null);
    if (message == null) {
      return CompletableFuture.completedFuture(null);
    }

    return this.announcePunishmentMessage(punishment, message);
  }

  @Override
  public @NonNull Component errorNoBan(final @NonNull BanUser user) {
    return this.parseWithTarget(this.cfg.errors.noBan, user);
  }

  @Override
  public @NonNull Component errorNoMute(final @NonNull BanUser user) {
    return this.parseWithTarget(this.cfg.errors.noMute, user);
  }

  @SuppressWarnings("UnstableApiUsage")
  @Override
  public @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Component>> formatHistory(
      final @NonNull ImmutableCollection<@NonNull Punishment> punishments,
      final @NonNull BanUser target) {
    final ImmutableList.Builder<Component> builder = ImmutableList.builderWithExpectedSize(punishments.size());
    builder.add(MiniMessage.get().parse(
        this.cfg.commands.historyHeader,

        "targetName", target.getUsername(),
        "targetUuid", target.getUuid().toString(),

        "amount", Integer.toString(punishments.size())));
    if (punishments.isEmpty()) {
      // Don't do wasteful allocations.
      return CompletableFuture.completedFuture(builder.build());
    }

    CompletableFuture<Component> future = CompletableFuture.completedFuture(null);
    for (final Punishment punishment : punishments) {
      if (punishment.getPunishmentType() == PunishmentType.NOTE) {
        // Don't show history of notes.
        continue;
      }

      future = future.thenCombine(
          this.formatMessageWith(this.cfg.commands.historyEntry, punishment),
          (first, second) -> {
            builder.add(second);
            return null;
          });
    }

    return future.thenApply($ -> builder.build());
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Component> formatApplication(final @NonNull Punishment punishment) {
    final String message = punishment.getPunishmentType().getApplicationMessage(
        punishment.getReason().isPresent()).map(key -> key.map(this.cfg)).orElse(null);
    if (message == null) {
      return CompletableFuture.completedFuture(Component.empty());
    }

    return this.formatMessageWith(message, punishment);
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Component> formatMessageWith(
      final @NonNull String message,
      final @NonNull Punishment punishment) {
    return this.userService.getUser(punishment.getTarget())
        .thenApply(opt ->
            opt.orElseThrow(() -> new IllegalArgumentException("Target of punishment cannot be unknown."))
        )
        .thenCombine(
            this.userService.getUser(punishment.getPunisher()),
            (target, punisher) -> this.formatMessageWith(
                punishment,
                message,
                punisher.orElseThrow(() -> new IllegalArgumentException("Punisher of punishment cannot be unknown")),
                target
            )
        );
  }

  @Override
  public @NonNull Component formatMessageWith(
      final @NonNull Punishment punishment,
      final @NonNull String message,
      final @NonNull BanUser punisher,
      final @NonNull BanUser target) {
    final String punishmentVerb = punishment.getPunishmentType().getVerbPastTense().map(this.cfg);

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
            "punishmentType", punishment.getPunishmentType().name(),
            "punishmentVerb", punishmentVerb,

            "expiry", !punishment.currentlyApplies()
                ? this.cfg.formatting.isLifted
                : punishment.isPermanent()
                    ? this.cfg.formatting.never
                    : DurationFormatUtils.formatDurationHMS(punishment.getExpiration() - System.currentTimeMillis()),
            "duration", punishment.isPermanent()
                ? this.cfg.formatting.permanently
                : this.cfg.formatting.durationFormat
                    .replace("<duration>",
                        DurationFormatUtils.formatDurationWords(
                            punishment.getExpiration() - System.currentTimeMillis(),
                            false,
                            false
                        ))
        );
  }

  private @NonNull Component parseWithTarget(
      final @NonNull String msg,
      final @NonNull BanUser target) {
    return MiniMessage.get().parse(
        msg,

        "targetName", target.getUsername(),
        "targetUuid", target.getUuid().toString()
    );
  }

  private @NonNull CompletableFuture<@Nullable Void> announcePunishmentMessage(
      final @NonNull Punishment punishment,
      final @NonNull String message) {
    final String permission = punishment.getPunishmentType().getNotificationPermission().orElse(null);
    return this.formatMessageWith(message, punishment)
        .thenApply(component -> {
          this.proxyServer.getConsoleCommandSource().sendMessage(Identity.nil(), component);
          for (final Player player : this.proxyServer.getAllPlayers()) {
            if (permission != null && !player.hasPermission(permission)) {
              continue;
            }

            player.sendMessage(Identity.nil(), component);
          }

          return null;
        });
  }
}
