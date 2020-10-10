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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IUserService;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class ImplMessageService implements IMessageService {
  private final @NonNull IUserService userService;
  private final @NonNull MessagesConfig cfg;

  @Inject
  public ImplMessageService(final @NonNull IUserService userService,
      final @NonNull MessagesConfig messagesConfig) {
    this.userService = userService;
    this.cfg = messagesConfig;
  }

  @Override
  public @NonNull Component errorNoBan(final @NonNull BanUser user) {
    return this.parseWithTarget(this.cfg.errors.noBan, user);
  }

  @Override
  public @NonNull Component errorNoMute(final @NonNull BanUser user) {
    return this.parseWithTarget(this.cfg.errors.noMute, user);
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
            // CHECKSTYLE:OFF - FIXME
            (target, punisher) -> this.formatMessageWith(
                // CHECKSTYLE:ON
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
}
