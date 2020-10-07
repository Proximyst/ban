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

public final class BanPermissions {
  private static final String BASE = "ban.";
  private static final String BASE_COMMANDS = BASE + "commands.";
  private static final String BASE_NOTIFY = BASE + "notify.";

  public static final String COMMAND_BAN = BASE_COMMANDS + "ban";
  public static final String COMMAND_UNBAN = BASE_COMMANDS + "unban";
  public static final String COMMAND_KICK = BASE_COMMANDS + "kick";
  public static final String COMMAND_MUTE = BASE_COMMANDS + "mute";

  public static final String NOTIFY_BAN = BASE_NOTIFY + "ban";
  public static final String NOTIFY_KICK = BASE_NOTIFY + "kick";
  public static final String NOTIFY_MUTE = BASE_NOTIFY + "mute";
  public static final String NOTIFY_WARN = BASE_NOTIFY + "warn";

  private BanPermissions() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }
}
