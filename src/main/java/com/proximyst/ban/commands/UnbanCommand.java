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
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.cloud.BanUserArgument;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnbanCommand extends BaseCommand {
  @Inject
  public UnbanCommand(final @NonNull BanPlugin main) {
    super(main);
  }

  @Override
  public void register(final @NonNull VelocityCommandManager<@NonNull CommandSource> commandManager) {
    commandManager.command(commandManager.commandBuilder("unban")
        .withPermission(BanPermissions.COMMAND_UNBAN)
        .argument(BanUserArgument.of("target", this.getMain()))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<CommandSource> ctx) {
    final @NonNull BanUser target = ctx.get("target");

    this.getMain().getPunishmentManager().getActiveBan(target.getUuid())
        .thenAccept(punishmentOptional -> {
          final Punishment punishment = punishmentOptional.orElse(null);
          if (punishment == null) {
            ctx.getSender().sendMessage(this.getMain().getMessageManager().errorNoBan(target));
            return;
          }

          punishment.setLiftedBy(CommandUtils.getSourceUuid(ctx.getSender()));
          punishment.broadcast(this.getMain());
          this.getMain().getSchedulerExecutor()
              .execute(() -> this.getMain().getDataInterface().addPunishment(punishment));
        });
  }
}
