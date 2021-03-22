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
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.IUserService;
import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnbanCommand extends BaseCommand {
  private final @NonNull BanExceptionalFutureLogger<?> banExceptionalFutureLogger;
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IMessageService messageService;
  private final @NonNull IUserService userService;

  private final BanIdentityArgument<? extends BanIdentity> argTarget;

  @Inject
  UnbanCommand(final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService,
      final @NonNull IMessageService messageService,
      final @NonNull IUserService userService) {
    this.banExceptionalFutureLogger = banExceptionalFutureLoggerFactory.createLogger(this.getClass());
    this.punishmentService = punishmentService;
    this.messageService = messageService;
    this.userService = userService;

    this.argTarget = cloudArgumentFactory.banIdentity("target", true, BanIdentity.class);
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull IBanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("unban")
        .permission(BanPermissions.COMMAND_UNBAN)
        .argument(this.argTarget)
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<IBanAudience> ctx) {
    final BanIdentity target = ctx.get(this.argTarget);

    this.messageService.feedbackUnban(ctx.getSender(), target);

    this.punishmentService.getActiveBan(target)
        .thenAccept(optExisting -> {
          final Punishment punishment = optExisting.orElse(null);
          if (punishment == null) {
            this.messageService.errorNoActiveBan(ctx.getSender(), target);
            return;
          }

          this.punishmentService.liftPunishment(punishment, ctx.getSender().uuid())
              .thenAccept(this.punishmentService::announcePunishment)
              .exceptionally(this.banExceptionalFutureLogger.cast());
        })
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }
}
