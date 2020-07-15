package com.proximyst.ban.utils;

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.commands.helper.argument.ArgumentReader;
import com.proximyst.ban.model.Punishment;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandUtils {
  private CommandUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  public static ImmutableList<String> suggestPlayerNames(
      @NonNull ProxyServer server,
      @NonNull ArgumentReader arguments
  ) {
    return suggestPlayerNames(server, arguments, false);
  }

  public static ImmutableList<String> suggestPlayerNames(
      @NonNull ProxyServer server,
      @NonNull ArgumentReader arguments,
      boolean silentFlag
  ) {
    return suggestPlayerNames(
        server,
        arguments.tryPop().map(String::toLowerCase).orElse(""),
        silentFlag
    );
  }

  private static ImmutableList<String> suggestPlayerNames(
      @NonNull ProxyServer server,
      @NonNull String name,
      boolean silentFlag
  ) {
    if (silentFlag && "-s".startsWith(name.toLowerCase())) {
      return ImmutableList.<String>builder()
          .add("-s")
          .addAll(suggestPlayerNames(server, name, false))
          .build();
    }

    return server.getAllPlayers().stream()
        .map(Player::getUsername)
        .filter(it -> it.toLowerCase().startsWith(name))
        .sorted(String::compareToIgnoreCase)
        .collect(ImmutableList.toImmutableList());
  }

  @NonNull
  public static String getSourceName(@NonNull CommandSource source) {
    return source instanceof Player ? ((Player) source).getUsername() : "CONSOLE";
  }

  @NonNull
  public static UUID getSourceUuid(@NonNull CommandSource source) {
    return source instanceof Player ? ((Player) source).getUniqueId() : Punishment.CONSOLE_UUID;
  }
}
