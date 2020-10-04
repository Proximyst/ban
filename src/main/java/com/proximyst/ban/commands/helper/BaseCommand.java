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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class BaseCommand {
  @NonNull
  private final BanPlugin main;

  public BaseCommand(@NonNull final BanPlugin main) {
    this.main = main;
  }

  public abstract void register(@NonNull final CommandManager commandManager);

  @NonNull
  protected <T> Optional<T> getOptionalArgument(@NonNull final ThrowingSupplier<T, CommandSyntaxException> supplier)
      throws CommandSyntaxException {
    try {
      return Optional.of(supplier.get());
    } catch (final IllegalArgumentException ignored) {
      // No such argument defined.
      return Optional.empty();
    }
  }

  @NonNull
  protected LiteralArgumentBuilder<CommandSource> literal(@NonNull final String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  @NonNull
  protected <T> RequiredArgumentBuilder<CommandSource, T> argRequired(
      @NonNull final String name,
      @NonNull final ArgumentType<T> type
  ) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  @NonNull
  protected Command<CommandSource> execute(
      @NonNull final ThrowingConsumer<CommandContext<CommandSource>, Exception> block
  ) {
    return ctx -> {
      try {
        block.accept(ctx);
        return 1;
      } catch (final CommandSyntaxException ex) {
        ctx.getSource().sendMessage(Component.text(ex.getMessage(), NamedTextColor.RED));
        return -1;
      } catch (final Exception ex) {
        this.getMain().getLogger().warn("Could not execute command.", ex);
        ctx.getSource().sendMessage(Component.text(
            "An internal command execution error occurred. Please contact an administrator.",
            NamedTextColor.RED
        ));
        return -1;
      }
    };
  }

  @NonNull
  protected BanPlugin getMain() {
    return this.main;
  }
}
