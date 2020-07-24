package com.proximyst.ban;

public final class BanPermissions {
  private BanPermissions() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  private static final String BASE = "ban.";
  private static final String BASE_COMMANDS = BASE + "commands.";
  private static final String BASE_SILENT = ".silent";
  private static final String BASE_NOTIFY = BASE + "notify.";

  public static final String COMMAND_BAN = BASE_COMMANDS + "ban";
  public static final String COMMAND_BAN_SILENT = COMMAND_BAN + BASE_SILENT;

  public static final String NOTIFY_BAN = BASE_NOTIFY + "ban";
  public static final String NOTIFY_BAN_SILENT = NOTIFY_BAN + BASE_SILENT;
  public static final String NOTIFY_KICK = BASE_NOTIFY + "kick";
  public static final String NOTIFY_KICK_SILENT = NOTIFY_KICK + BASE_SILENT;
  public static final String NOTIFY_MUTE = BASE_NOTIFY + "mute";
  public static final String NOTIFY_MUTE_SILENT = NOTIFY_MUTE + BASE_SILENT;
  public static final String NOTIFY_WARN = BASE_NOTIFY + "warn";
  public static final String NOTIFY_WARN_SILENT = NOTIFY_WARN + BASE_SILENT;
}
