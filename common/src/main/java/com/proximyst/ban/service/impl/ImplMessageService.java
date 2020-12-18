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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IUserService;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ImplMessageService implements IMessageService {
  private final @NonNull IUserService userService;
  private final @NonNull MessagesConfig cfg;
  private final @NonNull IBanServer banServer;

  @Inject
  public ImplMessageService(final @NonNull IUserService userService,
      final @NonNull MessagesConfig messagesConfig,
      final @NonNull IBanServer banServer) {
    this.userService = userService;
    this.cfg = messagesConfig;
    this.banServer = banServer;
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> announceNewPunishment(final @NonNull Punishment punishment) {
    return punishment.getPunishmentType()
        .getBroadcastMessage(punishment.getReason().isPresent())
        .map(msg -> this.announcePunishmentMessage(punishment, msg))
        .orElseGet(() -> CompletableFuture.completedFuture(null));
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> announceLiftedPunishment(final @NonNull Punishment punishment) {
    return punishment.getPunishmentType()
        .getBroadcastLiftMessage()
        .map(msg -> this.announcePunishmentMessage(punishment, msg))
        .orElseGet(() -> CompletableFuture.completedFuture(null));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Override
  public @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Component>> formatHistory(
      final @NonNull ImmutableCollection<@NonNull Punishment> punishments,
      final @NonNull BanUser target) {
    final ImmutableList.Builder<Component> builder = ImmutableList.builderWithExpectedSize(punishments.size());
    builder.add(this.formatMessage(MessageKey.COMMANDS_HISTORY_HEADER,
        "targetName", target.getUsername(),
        "targetUuid", target.getUuid(),
        "amount", punishments.size())
        // We know there are only constant values here, no futures.
        // The future is therefore instantly finished, and we don't actually need to wait for it.
        // TODO(Proximyst): Make an internal function for formatting methods which are constant values?
        .join());
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
          this.formatMessage(MessageKey.COMMANDS_HISTORY_ENTRY, punishment),
          (first, second) -> {
            builder.add(second);
            return null;
          });
    }

    return future.thenApply($ -> builder.build());
  }

  @Override
  public @NonNull CompletableFuture<Component> formatMessage(final @Nullable MessageKey messageKey,
      final @Nullable Object @NonNull ... placeholders) {
    Preconditions.checkArgument(placeholders.length % 2 == 0, "There must be an event length for `placeholders`");

    if (messageKey == null) {
      return CompletableFuture.completedFuture(Component.empty());
    }

    final String message = messageKey.map(this.cfg);
    if (message.isEmpty()) {
      return CompletableFuture.completedFuture(Component.empty());
    }

    final String[] strings = new String[placeholders.length];
    for (int i = 0; i < placeholders.length; ++i) {
      final Object obj = placeholders[i];
      if (obj instanceof CompletableFuture) {
        // Alright, we have some placeholder that must be awaited!

        // First off, let's finish the resting placeholder string-ification.
        // We start with j = i+1 because we don't want the current object,
        // then we work our way up from there.
        // We also count the amount of futures we have, for use in #formatMessageFuture.
        int futures = 1;
        for (int j = i + 1; j < placeholders.length; ++j) {
          final Object rest = placeholders[j];
          if (rest instanceof CompletableFuture) {
            ++futures;
          }
        }

        return this.formatMessageFuture(message, futures, placeholders);
      }

      strings[i] = String.valueOf(obj);
    }

    return CompletableFuture.completedFuture(MiniMessage.get().parse(message, strings));
  }

  @Override
  public @NonNull CompletableFuture<Component> formatMessage(final @Nullable MessageKey messageKey,
      final @NonNull Punishment punishment,
      final @Nullable Object @NonNull ... placeholders) {
    if (messageKey == null) {
      return CompletableFuture.completedFuture(Component.empty());
    }

    return this.userService.getUser(punishment.getTarget())
        .thenApply(
            opt -> opt.orElseThrow(() -> new IllegalArgumentException("Target of punishment cannot be unknown.")))
        .thenCombine(
            this.userService.getUser(punishment.getPunisher())
                .thenApply(opt ->
                    opt.orElseThrow(() -> new IllegalArgumentException("Punisher of punishment cannot be unknown."))),
            (target, punisher) -> ObjectArrays.concat(placeholders,
                this.createPlaceholders(punishment, target, punisher), Object.class))
        .thenCompose(p -> this.formatMessage(messageKey, p));
  }

  /**
   * @param message      The message to pass to MiniMessage.
   * @param futureCount  The amount of futures in the placeholders array.
   * @param placeholders The placeholders in an array which is equivalent to {@code (String,
   *                     String|CompletableFuture)[]}.
   * @return The finished component.
   */
  private @NonNull CompletableFuture<@NonNull Component> formatMessageFuture(final @NonNull String message,
      final @Positive int futureCount,
      final @Nullable Object @NonNull ... placeholders) {
    final CompletableFuture<?>[] futures = new CompletableFuture<?>[futureCount];

    // We know how many futures there are, but we also need to actually find them.
    int futureIdx = 0;
    for (final Object placeholder : placeholders) {
      if (placeholder instanceof CompletableFuture) {
        futures[futureIdx++] = (CompletableFuture<?>) placeholder;
        if (futureIdx == futureCount) {
          break;
        }
      }
    }

    // We now need to know when all the futures are done.
    // Luckily, we can do that with helper methods in CompletableFuture.
    final CompletableFuture<?> allDone = CompletableFuture.allOf(futures);

    return allDone.thenApply($ -> {
      // We now know all the futures are done.
      // We'll need to unwrap them, then pass to MiniMessage.
      final String[] strings = new String[placeholders.length];
      for (int i = 0; i < placeholders.length; ++i) {
        final Object obj = placeholders[i];
        if (obj instanceof CompletableFuture) {
          strings[i] = String.valueOf(((CompletableFuture<?>) obj).join());
        } else {
          strings[i] = String.valueOf(obj);
        }
      }

      return MiniMessage.get().parse(message, strings);
    });
  }

  private @NonNull CompletableFuture<@Nullable Void> announcePunishmentMessage(final @NonNull Punishment punishment,
      final @NonNull MessageKey messageKey) {
    final String permission = punishment.getPunishmentType().getNotificationPermission().orElse(null);
    return this.formatMessage(messageKey, punishment)
        .thenApply(component -> {
          this.banServer.consoleAudience().sendMessage(Identity.nil(), component);
          for (final IBanAudience audience : this.banServer.onlineAudiences()) {
            if (permission != null && !audience.hasPermission(permission)) {
              continue;
            }

            audience.sendMessage(Identity.nil(), component);
          }

          return null;
        });
  }

  private @Nullable Object @NonNull [] createPlaceholders(final @NonNull Punishment punishment,
      final @NonNull BanUser target, final @NonNull BanUser punisher) {
    return new Object[]{
        "targetName", target.getUsername(),
        "targetUuid", target.getUuid().toString(),

        "punisherName", punisher.getUsername(),
        "punisherUuid", punisher.getUuid().toString(),

        "punishmentId", punishment.getId().orElse(-1L).toString(),
        "punishmentDate", SimpleDateFormat.getDateInstance().format(punishment.getDate()),
        "reason", punishment.getReason().map(MiniMessage.get()::escapeTokens).orElse("No reason specified"),
        "punishmentType", punishment.getPunishmentType().name(),
        "punishmentVerb", punishment.getPunishmentType().getVerbPastTense().map(this.cfg),

        "expiry", !punishment.currentlyApplies()
        ? this.cfg.formatting.isLifted
        : punishment.isPermanent()
            ? this.cfg.formatting.never
            : DurationFormatUtils
                .formatDurationHMS(punishment.getExpiration() - System.currentTimeMillis()),
        "duration", punishment.isPermanent()
        ? this.cfg.formatting.permanently
        : this.cfg.formatting.durationFormat
            .replace("<duration>",
                DurationFormatUtils.formatDurationWords(
                    punishment.getExpiration() - System.currentTimeMillis(),
                    false,
                    false
                ))};
  }
}
