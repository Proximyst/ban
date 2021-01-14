//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
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
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.commands.cloud.BanIdentityArgument;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class HistoryCommand extends BaseCommand {
  private final @NonNull BanExceptionalFutureLogger<?> banExceptionalFutureLogger;
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IMessageService messageService;

  private final BanIdentityArgument<? extends BanIdentity> argTarget;

  @Inject
  HistoryCommand(final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService,
      final @NonNull IMessageService messageService) {
    this.banExceptionalFutureLogger = banExceptionalFutureLoggerFactory.createLogger(this.getClass());
    this.punishmentService = punishmentService;
    this.messageService = messageService;

    this.argTarget = cloudArgumentFactory.banIdentity("target", true);
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull IBanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("history")
        .permission(BanPermissions.COMMAND_HISTORY)
        .argument(this.argTarget)
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<IBanAudience> ctx) {
    final BanIdentity target = ctx.get(this.argTarget);

    this.messageService.feedbackHistory(ctx.getSender(), target);

    this.punishmentService.getPunishments(target)
        .thenAccept(punishments -> {
          this.messageService.feedbackHistoryHeader(ctx.getSender(), target, punishments.size());
          punishments.forEach(punishment -> this.messageService.feedbackHistoryEntry(ctx.getSender(), punishment));
        })
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }
}
