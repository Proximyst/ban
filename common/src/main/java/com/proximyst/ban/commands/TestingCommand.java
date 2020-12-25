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
import com.proximyst.ban.factory.IMessageFactory;
import com.proximyst.ban.platform.IBanAudience;
import org.checkerframework.checker.nullness.qual.NonNull;

@Deprecated // FIXME(Proximyst)
public class TestingCommand extends BaseCommand {
  private final @NonNull IMessageFactory messageFactory;
  private final @NonNull ICloudArgumentFactory cloudArgumentFactory;

  @Inject
  public TestingCommand(final @NonNull IMessageFactory messageFactory,
      final @NonNull ICloudArgumentFactory cloudArgumentFactory) {
    this.messageFactory = messageFactory;
    this.cloudArgumentFactory = cloudArgumentFactory;
  }

  @Override
  public void register(final @NonNull CommandManager<@NonNull IBanAudience> commandManager) {
    commandManager.command(commandManager.commandBuilder("testing")
        .permission(BanPermissions.COMMAND_KICK)
        .argument(this.cloudArgumentFactory.banUser("target", true))
        .argument(StringArgument.optional("reason", StringArgument.StringMode.GREEDY))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<IBanAudience> ctx) {
    this.messageFactory.staticMessage(MessageKey.COMMANDS_FEEDBACK_UNBAN)
        .send(ctx.getSender());
    this.messageFactory.placeholderMessage(MessageKey.COMMANDS_FEEDBACK_UNBAN,
        this.messageFactory.staticComponent("targetName", ctx.getSender().username()))
        .send(ctx.getSender());
  }
}
