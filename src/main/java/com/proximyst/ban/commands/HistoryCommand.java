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
import com.proximyst.ban.service.IPunishmentService;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class HistoryCommand extends BaseCommand {
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;
  private final @NonNull IPunishmentService punishmentService;

  @Inject
  public HistoryCommand(final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService) {
    this.cloudArgumentFactory = cloudArgumentFactory;
    this.punishmentService = punishmentService;
  }

  @Override
  public void register(final @NonNull VelocityCommandManager<@NonNull CommandSource> commandManager) {
    commandManager.command(commandManager.commandBuilder("history")
        .permission(BanPermissions.COMMAND_HISTORY)
        .argument(this.cloudArgumentFactory.banUser("target", true))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<CommandSource> ctx) {
    final BanUser target = ctx.get("target");

    this.punishmentService.getPunishments(target.getUuid())
        .thenAccept(punishments -> {
          // TODO: Customisable messages
          ctx.getSender().sendMessage(Component.text()
              .append(Component.text("Found ", NamedTextColor.YELLOW))
              .append(Component.text(punishments.size(), NamedTextColor.GOLD))
              .append(Component.text(" punishments for ", NamedTextColor.YELLOW))
              .append(Component.text(target.getUsername(), NamedTextColor.GOLD))
              .append(Component.text(".", NamedTextColor.YELLOW)));
          for (final Punishment punishment : punishments) {
            // TODO: Proper message
            ctx.getSender().sendMessage(Component.text(punishment.toString()));
          }
        });
  }
}
