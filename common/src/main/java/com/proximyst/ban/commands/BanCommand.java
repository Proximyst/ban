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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.google.inject.Inject;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.platform.BanAudience;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanCommand extends BaseCommand {
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IMessageService messageService;

  @Inject
  public BanCommand(final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService,
      final @NonNull IMessageService messageService) {
    this.cloudArgumentFactory = cloudArgumentFactory;
    this.punishmentService = punishmentService;
    this.messageService = messageService;
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull BanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("ban")
        .permission(BanPermissions.COMMAND_BAN)
        .argument(this.cloudArgumentFactory.banUser("target", true))
        .argument(StringArgument.optional("reason", StringArgument.StringMode.GREEDY))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<@NonNull ? extends BanAudience> ctx) {
    final BanUser target = ctx.get("target");
    final @Nullable String reason = ctx.getOrDefault("reason", null);

    this.messageService.sendFormattedMessage(ctx.getSender(), Identity.nil(), MessageKey.COMMANDS_FEEDBACK_BAN,
        "targetName", target.getUsername(),
        "targetUuid", target.getUuid());

    // We have to lift their previous punishment.
    this.punishmentService.getActiveBan(target.getUuid())
        .thenAccept(optExisting -> {
          optExisting.ifPresent(existing -> {
            existing.setLiftedBy(ctx.getSender().uuid());
            this.punishmentService.savePunishment(existing);
          });

          final Punishment punishment =
              new PunishmentBuilder()
                  .type(PunishmentType.BAN)
                  .punisher(ctx.getSender().uuid())
                  .target(target.getUuid())
                  .reason(reason)
                  .build();
          this.punishmentService.savePunishment(punishment);
          this.punishmentService.applyPunishment(punishment);
          this.messageService.announceNewPunishment(punishment);
        });
  }
}