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

package com.proximyst.ban.commands.cloud;

import cloud.commandframework.CommandTree;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import com.velocitypowered.api.command.CommandSource;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ScheduledCommandExecutionCoordinator extends CommandExecutionCoordinator<CommandSource> {
  private final @NonNull Executor executor;
  private final @NonNull CommandExecutionCoordinator<CommandSource> deferee;

  public ScheduledCommandExecutionCoordinator(
      @NonNull final CommandTree<CommandSource> commandTree,
      @NonNull final Executor executor,
      @NonNull final CommandExecutionCoordinator<CommandSource> deferee
  ) {
    super(commandTree);
    this.executor = executor;
    this.deferee = deferee;
  }

  @Override
  public @NonNull CompletableFuture<CommandResult<@NonNull CommandSource>> coordinateExecution(
      @NonNull final CommandContext<CommandSource> commandContext,
      @NonNull final Queue<String> input
  ) {
    return CompletableFuture.supplyAsync(
        // CHECKSTYLE:OFF - FIXME
        () -> this.deferee.coordinateExecution(commandContext, input).join(),
        // CHECKSTYLE:ON
        this.executor
    );
  }
}
