package com.proximyst.ban.commands.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.CommandSource;
import java.util.Optional;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VelocityBrigadierUtils {
  private VelocityBrigadierUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static LiteralArgumentBuilder<CommandSource> literal(@NonNull String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  @NonNull
  public static <T> RequiredArgumentBuilder<CommandSource, T> requiredArg(
      @NonNull String name,
      @NonNull ArgumentType<T> type
  ) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  @NonNull
  public static <T> Optional<T> getOptionalArgument(Supplier<T> supplier) {
    try {
      return Optional.ofNullable(supplier.get());
    } catch (IllegalArgumentException ignored) {
      // No such argument!
      return Optional.empty();
    }
  }
}
