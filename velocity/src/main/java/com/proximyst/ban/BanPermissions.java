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

package com.proximyst.ban;

import org.checkerframework.checker.nullness.qual.NonNull;

@NonNull
public final class BanPermissions {
  private static final String BASE = "ban.";

  private static final String BASE_COMMANDS = BASE + "commands.";
  public static final String COMMAND_BAN = BASE_COMMANDS + "ban";
  public static final String COMMAND_UNBAN = BASE_COMMANDS + "unban";
  public static final String COMMAND_KICK = BASE_COMMANDS + "kick";
  public static final String COMMAND_MUTE = BASE_COMMANDS + "mute";
  public static final String COMMAND_UNMUTE = BASE_COMMANDS + "unmute";
  public static final String COMMAND_HISTORY = BASE_COMMANDS + "history";
  public static final String COMMAND_WARN = BASE_COMMANDS + "warn";
  public static final String COMMAND_UNWARN = BASE_COMMANDS + "unwarn";
  public static final String COMMAND_NOTE = BASE_COMMANDS + "note";
  public static final String COMMAND_LOCKDOWN = BASE_COMMANDS + "lockdown";

  private static final String BASE_NOTIFY = BASE + "notify.";
  public static final String NOTIFY_BAN = BASE_NOTIFY + "ban";
  public static final String NOTIFY_KICK = BASE_NOTIFY + "kick";
  public static final String NOTIFY_MUTE = BASE_NOTIFY + "mute";
  public static final String NOTIFY_WARN = BASE_NOTIFY + "warn";
  public static final String NOTIFY_LOCKDOWN = BASE_NOTIFY + "lockdown";

  private static final String BASE_BYPASS = BASE + "bypass.";
  public static final String BYPASS_LOCKDOWN = BASE_BYPASS + "lockdown";
  public static final String BYPASS_BAN = BASE_BYPASS + "ban";
  public static final String BYPASS_KICK = BASE_BYPASS + "kick";
  public static final String BYPASS_MUTE = BASE_BYPASS + "mute";

  private BanPermissions() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }
}
