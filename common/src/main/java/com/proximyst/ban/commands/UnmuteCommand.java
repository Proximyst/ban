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
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.platform.BanAudience;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnmuteCommand extends BaseCommand {
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IMessageService messageService;

  @Inject
  public UnmuteCommand(final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService,
      final @NonNull IMessageService messageService) {
    this.cloudArgumentFactory = cloudArgumentFactory;
    this.punishmentService = punishmentService;
    this.messageService = messageService;
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull BanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("unmute")
        .permission(BanPermissions.COMMAND_UNMUTE)
        .argument(this.cloudArgumentFactory.banUser("target", true))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<BanAudience> ctx) {
    final @NonNull BanUser target = ctx.get("target");

    this.messageService.sendFormattedMessage(ctx.getSender(), Identity.nil(), MessageKey.COMMANDS_FEEDBACK_UNMUTE,
        "targetName", target.getUsername(),
        "targetUuid", target.getUuid());

    this.punishmentService.getActiveMute(target.getUuid())
        .thenAccept(punishmentOptional -> {
          final Punishment punishment = punishmentOptional.orElse(null);
          if (punishment == null) {
            this.messageService.sendFormattedMessage(ctx.getSender(), Identity.nil(), MessageKey.ERROR_NO_ACTIVE_MUTE,
                "targetName", target.getUsername(),
                "targetUuid", target.getUuid());
            return;
          }

          punishment.setLiftedBy(ctx.getSender().uuid());
          this.punishmentService.savePunishment(punishment);
          this.messageService.announceLiftedPunishment(punishment);
        });
  }
}
