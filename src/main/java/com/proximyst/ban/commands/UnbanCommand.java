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

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnbanCommand extends BaseCommand {
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;
  private final @NonNull IMessageService messageService;
  private final @NonNull IPunishmentService punishmentService;

  @Inject
  public UnbanCommand(final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IMessageService messageService,
      final @NonNull IPunishmentService punishmentService) {
    this.cloudArgumentFactory = cloudArgumentFactory;
    this.messageService = messageService;
    this.punishmentService = punishmentService;
  }

  @Override
  public void register(final @NonNull VelocityCommandManager<@NonNull CommandSource> commandManager) {
    commandManager.command(commandManager.commandBuilder("unban")
        .permission(BanPermissions.COMMAND_UNBAN)
        .argument(this.cloudArgumentFactory.banUser("target", true))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<CommandSource> ctx) {
    final @NonNull BanUser target = ctx.get("target");

    this.punishmentService.getActiveBan(target.getUuid())
        .thenAccept(punishmentOptional -> {
          final Punishment punishment = punishmentOptional.orElse(null);
          if (punishment == null) {
            ctx.getSender().sendMessage(this.messageService.errorNoBan(target));
            return;
          }

          // TODO: Broadcast
          punishment.setLiftedBy(CommandUtils.getSourceUuid(ctx.getSender()));
          this.punishmentService.savePunishment(punishment);
        });
  }
}
