package com.proximyst.ban.commands.helper;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.proximyst.ban.data.IMojangApi;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.utils.StringUtils;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UserArgument {
  private static final SimpleCommandExceptionType EXPECTED_PLAYER
      = new SimpleCommandExceptionType(() -> "Expected player name/UUID");
  private static final DynamicCommandExceptionType INVALID_UUID
      = new DynamicCommandExceptionType(found -> () -> "Invalid UUID '" + found + "'");
  private static final DynamicCommandExceptionType INVALID_USERNAME
      = new DynamicCommandExceptionType(found -> () -> "Invalid player name '" + found + "'");

  private UserArgument() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static Optional<BanUser> getUser(
      @NonNull IMojangApi mojangApi,
      @NonNull ProxyServer proxyServer,
      @NonNull String input
  ) throws CommandSyntaxException {
    try {
      return mojangApi.getUser(getUuid(mojangApi, proxyServer, input))
          .getOrLoad().join();
    } catch (IllegalArgumentException ex) {
      throw new SimpleCommandExceptionType(ex::getMessage).create();
    }
  }

  @NonNull
  public static UUID getUuid(
      @NonNull IMojangApi mojangApi,
      @NonNull ProxyServer proxyServer,
      @NonNull String input
  ) throws CommandSyntaxException {
    @Nullable Player onlinePlayer = proxyServer.getAllPlayers()
        .stream()
        .filter(p -> p.getUsername().equalsIgnoreCase(input) || p.getUniqueId().toString().equalsIgnoreCase(input))
        .findAny()
        .orElse(null);
    if (onlinePlayer != null) {
      return onlinePlayer.getUniqueId();
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
        return UUID.fromString(input);
      } catch (IllegalArgumentException ex) {
        throw INVALID_UUID.create(input);
      }
    }

    if (input.length() == 32) {
      try {
        return UUID.fromString(StringUtils.rehyphenUuid(input));
      } catch (IllegalArgumentException ex) {
        throw INVALID_UUID.create(input);
      }
    }

    UUID uuid = mojangApi.getUuid(input).getOrLoad().join().orElse(null);
    if (uuid == null) {
      throw INVALID_USERNAME.create(input);
    }

    return uuid;
  }
}
