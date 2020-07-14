package com.proximyst.ban.commands.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BanCommandDispatcher extends CommandDispatcher<CommandSource> {
  private final BanPlugin main;

  public BanCommandDispatcher(@NonNull BanPlugin main) {
    this.main = main;
  }

  /**
   * Inject Brigadier commands into the player's commands.
   */
  @SuppressWarnings({"UnstableApiUsage", "rawtypes", "unchecked"})
  @Subscribe
  public void sendBrigadier(PlayerAvailableCommandsEvent event) {
    for (CommandNode child : getRoot().getChildren()) {
      event.getRootNode().addChild(child);
    }
  }

  /**
   * Register the proxying commands in Velocity.
   * <p>
   * They don't actually do anything other than delegate to Brigadier.
   */
  public void registerVelocityProxyCommands() {
    for (CommandNode<CommandSource> child : getRoot().getChildren()) {
      main.getProxyServer().getCommandManager().register(
          child.getName(),
          new VelocityBrigadierProxyCommand(this, child.getName())
      );
    }
  }

  /**
   * A proxy command for delegating all command events to the Brigadier backend.
   */
  static class VelocityBrigadierProxyCommand implements Command {
    private final BanCommandDispatcher dispatcher;
    private final String name;

    public VelocityBrigadierProxyCommand(BanCommandDispatcher dispatcher, String name) {
      this.dispatcher = dispatcher;
      this.name = name;
    }

    @Override
    public void execute(CommandSource source, @NonNull String[] args) {
      dispatcher.main.getProxyServer().getScheduler().buildTask(
          dispatcher.main,
          () -> {
            try {
              dispatcher.execute(getBrigadierInput(args), source);
            } catch (CommandSyntaxException ex) {
              ex.printStackTrace();
              source.sendMessage(
                  source instanceof Player
                      ? TranslatableComponent.of("command.unknown.command", NamedTextColor.RED)
                      : TextComponent.of("Unknown or incomplete command, see below for error", NamedTextColor.RED)
              );
              source.sendMessage(TextComponent.of(ex.getMessage(), NamedTextColor.RED));
            } catch (RuntimeException ex) {
              source.sendMessage(TextComponent.of("An unexpected error occurred.", NamedTextColor.RED));
              dispatcher.main.getLogger().warn(
                  "An error occurred while executing the command {} for {}.",
                  name,
                  CommandUtils.getSourceName(source)
              );
              dispatcher.main.getLogger().warn("Error:", ex);
            }
          }
      ).schedule();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(CommandSource source, @NonNull String[] currentArgs) {
      return dispatcher.getCompletionSuggestions(
          dispatcher.parse(getBrigadierInput(currentArgs), source)
      )
          .thenApply(suggestions ->
              suggestions.getList()
                  .stream()
                  .map(Suggestion::getText)
                  .collect(Collectors.toList())
          );
    }

    @Override
    public List<String> suggest(CommandSource source, @NonNull String[] currentArgs) {
      new Exception("Sync suggest called, please nag authors below").printStackTrace();
      return suggestAsync(source, currentArgs).join(); // Just don't do this, stupid.
    }

    @Override
    public boolean hasPermission(CommandSource source, @NonNull String[] args) {
      // Let Brigadier requirements handle this.
      return true;
    }

    private String getBrigadierInput(@NonNull String[] args) {
      if (args.length == 0) {
        return name;
      }

      return name + " " + String.join(" ", args);
    }
  }
}
