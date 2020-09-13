package com.proximyst.ban.commands.helper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.utils.ThrowingConsumer;
import com.proximyst.ban.utils.ThrowingSupplier;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import java.util.Optional;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class BaseCommand {
  @NonNull
  private final BanPlugin main;

  public BaseCommand(@NonNull BanPlugin main) {
    this.main = main;
  }

  public abstract void register(@NonNull CommandManager commandManager);

  @NonNull
  protected <T> Optional<T> getOptionalArgument(@NonNull ThrowingSupplier<T, CommandSyntaxException> supplier)
      throws CommandSyntaxException {
    try {
      return Optional.of(supplier.get());
    } catch (IllegalArgumentException ignored) {
      // No such argument defined.
      return Optional.empty();
    }
  }

  @NonNull
  protected LiteralArgumentBuilder<CommandSource> literal(@NonNull String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  @NonNull
  protected <T> RequiredArgumentBuilder<CommandSource, T> argRequired(
      @NonNull String name,
      @NonNull ArgumentType<T> type
  ) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  @NonNull
  protected Command<CommandSource> execute(
      @NonNull ThrowingConsumer<CommandContext<CommandSource>, Exception> block
  ) {
    return ctx -> {
      try {
        block.accept(ctx);
        return 0;
      } catch (CommandSyntaxException ex) {
        ctx.getSource().sendMessage(TextComponent.of(ex.getMessage(), NamedTextColor.RED));
        return -1;
      } catch (Exception ex) {
        getMain().getLogger().warn("Could not execute command.", ex);
        ctx.getSource().sendMessage(TextComponent.of(
            "An internal command execution error occurred. Please contact an administrator.",
            NamedTextColor.RED
        ));
        return -1;
      }
    };
  }

  @NonNull
  protected BanPlugin getMain() {
    return main;
  }
}
