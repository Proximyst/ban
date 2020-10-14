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
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class HistoryCommand extends BaseCommand {
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IMessageService messageService;
  private final @NonNull MessagesConfig messagesConfig;

  @Inject
  public HistoryCommand(final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService,
      final @NonNull IMessageService messageService,
      final @NonNull MessagesConfig messagesConfig) {
    this.cloudArgumentFactory = cloudArgumentFactory;
    this.punishmentService = punishmentService;
    this.messageService = messageService;
    this.messagesConfig = messagesConfig;
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

    ctx.getSender().sendMessage(Identity.nil(), MiniMessage.get().parse(
        this.messagesConfig.commands.historyFeedback,

        "targetName", target.getUsername(),
        "targetUuid", target.getUuid().toString()
    ));

    this.punishmentService.getPunishments(target.getUuid())
        .thenCompose(punishments -> this.messageService.formatHistory(punishments, target))
        .thenAccept(messages -> {
          for (final Component message : messages) {
            ctx.getSender().sendMessage(Identity.nil(), message);
          }
        });
  }
}
