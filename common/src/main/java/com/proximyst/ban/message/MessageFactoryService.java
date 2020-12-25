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

package com.proximyst.ban.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.factory.IMessageFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IUserService;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

// The existence of this class is because of this issue:
// <https://github.com/google/guice/issues/1345>.
@Singleton
public final class MessageFactoryService {
  private final @NonNull IMessageFactory messageFactory;
  private final @NonNull IMessageService messageService;
  private final @NonNull MessagesConfig messagesConfig;
  private final @NonNull IUserService userService;

  @Inject
  public MessageFactoryService(final @NonNull IMessageFactory messageFactory,
      final @NonNull IMessageService messageService,
      final @NonNull MessagesConfig messagesConfig,
      final @NonNull IUserService userService) {
    this.messageFactory = messageFactory;
    this.messageService = messageService;
    this.messagesConfig = messagesConfig;
    this.userService = userService;
  }

  // <editor-fold desc="Commands" defaultstate="collapsed">
  public @NonNull IMessage commandsFeedbackBan(final @NonNull BanUser target) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_BAN,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()));
  }

  public @NonNull IMessage commandsFeedbackKick(final @NonNull BanUser target) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_KICK,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()));
  }

  public @NonNull IMessage commandsFeedbackHistory(final @NonNull BanUser target) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_HISTORY,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()));
  }

  public @NonNull IMessage commandsFeedbackMute(final @NonNull BanUser target) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_MUTE,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()));
  }

  public @NonNull IMessage commandsFeedbackUnmute(final @NonNull BanUser target) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_UNMUTE,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()));
  }

  public @NonNull IMessage commandsFeedbackUnban(final @NonNull BanUser target) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_UNBAN,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()));
  }

  public @NonNull IMessage commandsHistoryHeader(final @NonNull BanUser target, final @NonNegative int amount) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_HISTORY_HEADER,
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()),
        this.messageFactory.staticComponent("amount", Integer.toString(amount)));
  }

  public @NonNull IMessage commandsHistoryEntry(final @NonNull Punishment punishment) {
    return this.messageFactory.placeholderMessage(MessageKey.COMMANDS_HISTORY_ENTRY,
        this.messageFactory.componentComponent(this.createPlaceholders(punishment)));
  }
  // </editor-fold>

  private @NonNull CompletableFuture<@NonNull IMessageComponent @NonNull []> createPlaceholders(
      final @NonNull Punishment punishment) {
    return this.userService.getUser(punishment.getTarget())
        .thenApply(
            opt -> opt.orElseThrow(() -> new IllegalArgumentException("Target of punishment cannot be unknown.")))
        .thenCombine(this.userService.getUser(punishment.getPunisher())
                .thenApply(opt -> opt
                    .orElseThrow(() -> new IllegalArgumentException("Punisher of punishment cannot be unknown."))),
            (target, punisher) -> this.createPlaceholders(punishment, target, punisher));
  }

  private @NonNull IMessageComponent @NonNull [] createPlaceholders(final @NonNull Punishment punishment,
      final @NonNull BanUser target, final @NonNull BanUser punisher) {
    final IMessageComponent expiry =
        !punishment.currentlyApplies()
            ? this.messageFactory.keyComponent("expiry", MessageKey.FORMATTING_LIFTED)
            : punishment.isPermanent()
                ? this.messageFactory.keyComponent("expiry", MessageKey.FORMATTING_NEVER)
                : this.messageFactory.staticComponent("expiry",
                    DurationFormatUtils.formatDurationHMS(punishment.getExpiration() - System.currentTimeMillis()));
    final IMessageComponent duration = punishment.isPermanent()
        ? this.messageFactory.keyComponent("duration", MessageKey.FORMATTING_PERMANENTLY)
        : this.messageFactory.staticComponent("duration", MessageKey.FORMATTING_DURATION.map(this.messagesConfig)
            .replace("<duration>",
                DurationFormatUtils.formatDurationWords(
                    punishment.getExpiration() - System.currentTimeMillis(),
                    false,
                    false
                )));

    return new IMessageComponent[]{
        this.messageFactory.staticComponent("targetName", target.getUsername()),
        this.messageFactory.staticComponent("targetUuid", target.getUuid().toString()),

        this.messageFactory.staticComponent("punisherName", punisher.getUsername()),
        this.messageFactory.staticComponent("punisherUuid", punisher.getUuid().toString()),

        this.messageFactory.staticComponent("punishmentId", punishment.getId().orElse(-1L).toString()),
        this.messageFactory.staticComponent("punishmentDate",
            SimpleDateFormat.getDateInstance().format(punishment.getDate())),
        this.messageFactory.staticComponent("reason",
            punishment.getReason().map(MiniMessage.get()::escapeTokens).orElse("No reason specified")),
        this.messageFactory.staticComponent("punishmentType", punishment.getPunishmentType().name()),
        this.messageFactory.keyComponent("punishmentVerb", punishment.getPunishmentType().getVerbPastTense()),

        expiry,
        duration,
    };
  }
}
