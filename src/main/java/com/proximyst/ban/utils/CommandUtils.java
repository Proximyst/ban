package com.proximyst.ban.utils;

import com.proximyst.ban.model.BanUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandUtils {
  private CommandUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static String getSourceName(@NonNull CommandSource source) {
    return source instanceof Player ? ((Player) source).getUsername() : BanUser.CONSOLE.getUsername();
  }

  @NonNull
  public static UUID getSourceUuid(@NonNull CommandSource source) {
    return source instanceof Player ? ((Player) source).getUniqueId() : BanUser.CONSOLE.getUuid();
  }
}
