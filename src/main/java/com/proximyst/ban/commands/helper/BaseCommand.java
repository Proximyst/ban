package com.proximyst.ban.commands.helper;

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.helper.argument.ArgumentReader;
import com.proximyst.ban.commands.helper.exception.IllegalCommandException;
import com.proximyst.ban.commands.helper.exception.UsageException;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class BaseCommand implements Command {
  @NonNull
  private final String usage;

  @NonNull
  private final BanPlugin main;

  public BaseCommand(@NonNull String usage, @NonNull BanPlugin main) {
    this.usage = Objects.requireNonNull(usage);
    this.main = main;
  }

  @Override
  public final void execute(CommandSource source, @NonNull String[] args) {
    getMain().getProxyServer().getScheduler().buildTask(getMain(),
        () -> {
          try {
            exec(source, new ArgumentReader(args));
          } catch (UsageException ignored) {
            // Only wants usage to be sent.
            sendUsage(source);
          } catch (IllegalCommandException ex) {
            source.sendMessage(TextComponent.builder("You entered an invalid command: ", NamedTextColor.RED)
                .append(ex.getMessage())
                .build());
            sendUsage(source);
          } catch (Exception ex) {
            source.sendMessage(TextComponent.of("An error occurred while executing this command."));
          }
        }).schedule();
  }

  @Override
  public final List<String> suggest(CommandSource source, @NonNull String[] currentArgs) {
    try {
      return suggest(source, new ArgumentReader(currentArgs));
    } catch (IllegalCommandException ignored) {
      return ImmutableList.of();
    }
  }

  @Override
  public final CompletableFuture<List<String>> suggestAsync(CommandSource source, @NonNull String[] currentArgs) {
    try {
      return suggestAsync(source, new ArgumentReader(currentArgs));
    } catch (IllegalCommandException ignored) {
      return CompletableFuture.completedFuture(ImmutableList.of());
    }
  }

  @Override
  public final boolean hasPermission(CommandSource source, @NonNull String[] args) {
    try {
      return hasPermission(source, new ArgumentReader(args));
    } catch (IllegalCommandException ignored) {
      return false;
    }
  }

  protected abstract void exec(@NonNull CommandSource source, @NonNull ArgumentReader args)
      throws IllegalCommandException;

  protected CompletableFuture<List<String>> suggestAsync(@NonNull CommandSource source, @NonNull ArgumentReader args)
      throws IllegalCommandException {
    return CompletableFuture.completedFuture(suggest(source, args));
  }

  protected List<String> suggest(@NonNull CommandSource source, @NonNull ArgumentReader args)
      throws IllegalCommandException {
    return ImmutableList.of();
  }

  protected abstract boolean hasPermission(@NonNull CommandSource source, @NonNull ArgumentReader args)
      throws IllegalCommandException;

  protected void sendUsage(CommandSource source) {
    source.sendMessage(TextComponent.builder(
        source instanceof Player
            ? "Usage: /"
            : "Usage: "
        , NamedTextColor.RED)
        .append(usage)
        .build());
  }

  @NonNull
  protected BanPlugin getMain() {
    return main;
  }

  @NonNull
  protected <T> Optional<T> getOptionalArgument(Supplier<T> supplier) {
    try {
      return Optional.ofNullable(supplier.get());
    } catch (IllegalCommandException ignored) {
      // No such argument, or something went wrong while parsing!
      return Optional.empty();
    }
  }
}
