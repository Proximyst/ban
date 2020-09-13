package com.proximyst.ban.commands.helper;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.manager.UserManager;
import com.proximyst.ban.utils.StringUtils;
import com.proximyst.ban.utils.ThrowableUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UserArgument {
  private static final SimpleCommandExceptionType EXPECTED_PLAYER
      = new SimpleCommandExceptionType(() -> "Expected player name/UUID.");
  private static final DynamicCommandExceptionType INVALID_UUID
      = new DynamicCommandExceptionType(found -> () -> "Invalid UUID '" + found + "'.");
  private static final DynamicCommandExceptionType INVALID_USERNAME
      = new DynamicCommandExceptionType(found -> () -> "Invalid player name '" + found + "'.");

  private UserArgument() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static CompletableFuture<UUID> getUuid(
      @NonNull UserManager userManager,
      @NonNull ProxyServer proxyServer,
      @NonNull String input
  ) throws CommandSyntaxException {
    @Nullable Player onlinePlayer = proxyServer.getAllPlayers()
        .stream()
        .filter(p -> p.getUsername().equalsIgnoreCase(input) || p.getUniqueId().toString().equalsIgnoreCase(input))
        .findAny()
        .orElse(null);
    if (onlinePlayer != null) {
      return CompletableFuture.completedFuture(onlinePlayer.getUniqueId());
    }

    if (input.length() < 3) {
      // Too short for a player name.
      throw EXPECTED_PLAYER.create();
    }
    if (input.length() > 16) {
      // This must be a UUID.
      if (input.length() != 32 && input.length() != 36) {
        // Neither UUID without dashes nor with dashes.
        throw EXPECTED_PLAYER.create();
      }
    }

    if (input.length() == 36) {
      try {
        return CompletableFuture.completedFuture(UUID.fromString(input));
      } catch (IllegalArgumentException ex) {
        throw INVALID_UUID.create(input);
      }
    }

    if (input.length() == 32) {
      try {
        return CompletableFuture.completedFuture(UUID.fromString(StringUtils.rehyphenUuid(input)));
      } catch (IllegalArgumentException ex) {
        throw INVALID_UUID.create(input);
      }
    }

    return userManager.getUser(input)
        .thenApply(res -> {
          try {
            return res
                .orElseThrow(() -> INVALID_USERNAME.create(input))
                .getUuid();
          } catch (CommandSyntaxException ex) {
            ThrowableUtils.sneakyThrow(ex);
            throw new RuntimeException();
          }
        });
  }

  @NonNull
  public static SuggestionProvider<CommandSource> createSuggestions(@NonNull BanPlugin main) {
    return (ctx, builder) -> {
      String input = builder.getRemaining().toLowerCase();

      for (Player player : main.getProxyServer().getAllPlayers()) {
        if (player.getUsername().toLowerCase().startsWith(input)) {
          builder.suggest(player.getUsername(), () -> "target");
        }
      }

      return builder.buildFuture();
    };
  }
}