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
