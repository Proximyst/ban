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

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.helper.BaseCommand;
import com.proximyst.ban.commands.helper.UserArgument;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanCommand extends BaseCommand {
  @Inject
  public BanCommand(@NonNull BanPlugin main) {
    super(main);
  }

  @Override
  public void register(@NonNull CommandManager commandManager) {
    commandManager.register(new BrigadierCommand(
        literal("ban")
            .requires(src -> src.hasPermission(BanPermissions.COMMAND_BAN))
            .then(argRequired("target", StringArgumentType.string())
                .suggests(UserArgument.createSuggestions(getMain()))
                .then(argRequired("reason", StringArgumentType.greedyString())
                    .executes(execute(this::execute)))
                .executes(execute(this::execute)))
    ));
  }

  private void execute(@NonNull CommandContext<CommandSource> ctx) throws CommandSyntaxException {
    @Nullable String reason = getOptionalArgument(() -> StringArgumentType.getString(ctx, "reason"))
        .map(String::trim)
        .filter(str -> !str.isEmpty())
        .orElse(null);
    UserArgument.getUuid(
        getMain().getUserManager(),
        getMain().getProxyServer(),
        StringArgumentType.getString(ctx, "target")
    ).thenComposeAsync(target -> getMain().getPunishmentManager().addPunishment(
        new PunishmentBuilder()
            .type(PunishmentType.BAN)
            .punisher(CommandUtils.getSourceUuid(ctx.getSource()))
            .target(target)
            .reason(reason)
    ), getMain().getSchedulerExecutor());
  }
}
