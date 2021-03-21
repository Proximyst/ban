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
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.arguments.standard.StringArgument.StringMode;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.types.tuples.Pair;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.IdentityMustExistException;
import com.proximyst.ban.commands.cloud.BanIdentityArgument;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.IUserService;
import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import java.util.Optional;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanCommand extends BaseCommand {
  private final @NonNull BanExceptionalFutureLogger<?> banExceptionalFutureLogger;
  private final @NonNull IPunishmentService punishmentService;
  private final @NonNull IMessageService messageService;
  private final @NonNull IUserService userService;

  private final BanIdentityArgument<? extends BanIdentity> argTarget;
  private final CommandArgument<IBanAudience, String> argReason;

  @Inject
  BanCommand(final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull ICloudArgumentFactory cloudArgumentFactory,
      final @NonNull IPunishmentService punishmentService,
      final @NonNull IMessageService messageService,
      final @NonNull IUserService userService) {
    this.banExceptionalFutureLogger = banExceptionalFutureLoggerFactory.createLogger(this.getClass());
    this.punishmentService = punishmentService;
    this.messageService = messageService;
    this.userService = userService;

    this.argTarget = cloudArgumentFactory.banIdentity("target", true, BanIdentity.class);
    this.argReason = StringArgument.optional("reason", StringMode.GREEDY);
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull IBanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("ban")
        .permission(BanPermissions.COMMAND_BAN)
        .argument(this.argTarget)
        .argument(this.argReason)
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<@NonNull IBanAudience> ctx) {
    final BanIdentity target = ctx.get(this.argTarget);
    final @Nullable String reason = ctx.getOrDefault(this.argReason, null);

    this.messageService.feedbackBan(ctx.getSender(), target);

    // We have to lift their previous punishment.
    this.punishmentService.getActiveBan(target)
        .thenCombine(this.userService.getUser(ctx.getSender().uuid()), Pair::of)
        .thenAccept(pair -> {
          final Optional<Punishment> optExisting = pair.getFirst();
          final UuidIdentity senderIdentity = pair.getSecond().orElseThrow(IdentityMustExistException::new);

          optExisting.ifPresent(this.punishmentService::liftPunishment);

          final PunishmentBuilder builder = new PunishmentBuilder()
              .type(PunishmentType.BAN)
              .punisher(senderIdentity)
              .target(target)
              .reason(reason);
          this.punishmentService.savePunishment(builder)
              .thenAccept(punishment -> {
                this.punishmentService.applyPunishment(punishment)
                    .exceptionally(this.banExceptionalFutureLogger.cast());
                this.punishmentService.announcePunishment(punishment);
              })
              .exceptionally(this.banExceptionalFutureLogger.cast());
        })
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }
}
