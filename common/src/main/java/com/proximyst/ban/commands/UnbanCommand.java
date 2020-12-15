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

package com.proximyst.ban.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import com.google.inject.Inject;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.platform.BanAudience;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnbanCommand extends BaseCommand {
  private final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory;
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;
  private final @NonNull IMessageService messageService;
  private final @NonNull IPunishmentService punishmentService;

  @Inject
  public UnbanCommand(final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IMessageService messageService,
      final @NonNull IPunishmentService punishmentService) {
    this.banExceptionalFutureLoggerFactory = banExceptionalFutureLoggerFactory;
    this.cloudArgumentFactory = cloudArgumentFactory;
    this.messageService = messageService;
    this.punishmentService = punishmentService;
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull BanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("unban")
        .permission(BanPermissions.COMMAND_UNBAN)
        .argument(this.cloudArgumentFactory.banUser("target", true))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<BanAudience> ctx) {
    final @NonNull BanUser target = ctx.get("target");

    this.messageService.sendFormattedMessage(ctx.getSender(), Identity.nil(), MessageKey.COMMANDS_FEEDBACK_UNBAN,
        "targetName", target.getUsername(),
        "targetUuid", target.getUuid())
        .exceptionally(this.banExceptionalFutureLoggerFactory.createLogger(this.getClass()));

    this.punishmentService.getActiveBan(target.getUuid())
        .thenAccept(punishmentOptional -> {
          final Punishment punishment = punishmentOptional.orElse(null);
          if (punishment == null) {
            this.messageService.sendFormattedMessage(ctx.getSender(), Identity.nil(), MessageKey.ERROR_NO_ACTIVE_BAN,
                "targetName", target.getUsername(),
                "targetUuid", target.getUuid())
                .exceptionally(this.banExceptionalFutureLoggerFactory.createLogger(this.getClass()));
            return;
          }

          punishment.setLiftedBy(ctx.getSender().uuid());
          this.punishmentService.savePunishment(punishment)
              .exceptionally(this.banExceptionalFutureLoggerFactory.createLogger(this.getClass()));
          this.messageService.announceLiftedPunishment(punishment)
              .exceptionally(this.banExceptionalFutureLoggerFactory.createLogger(this.getClass()));
        })
        .exceptionally(this.banExceptionalFutureLoggerFactory.createLogger(this.getClass()));
  }
}
