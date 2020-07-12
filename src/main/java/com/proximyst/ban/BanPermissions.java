package com.proximyst.ban;

public final class BanPermissions {
  private BanPermissions() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  private static final String BASE = "ban.";
  private static final String BASE_COMMANDS = BASE + "commands.";

  public static final String COMMAND_BAN = BASE_COMMANDS + "ban";
}
