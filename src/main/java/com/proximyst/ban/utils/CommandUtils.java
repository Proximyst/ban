package com.proximyst.ban.utils;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandUtils {
  private CommandUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  public static ImmutableList<String> suggestPlayerNames(@NonNull ProxyServer server, @NonNull String[] arguments) {
    return suggestPlayerNames(server, arguments.length == 0 ? "" : arguments[arguments.length - 1].toLowerCase());
  }

  private static ImmutableList<String> suggestPlayerNames(@NonNull ProxyServer server, @NonNull String name) {
    return server.getAllPlayers().stream()
        .map(Player::getUsername)
        .filter(it -> it.toLowerCase().startsWith(name))
        .sorted(String::compareToIgnoreCase)
        .collect(ImmutableList.toImmutableList());
  }
}
