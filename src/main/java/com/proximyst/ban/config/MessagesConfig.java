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

package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
@NonNull
public class MessagesConfig {
  @Setting
  public Errors errors = new Errors();

  @Setting
  public Broadcasts broadcasts = new Broadcasts();

  @Setting
  public Applications applications = new Applications();

  @Setting
  public Formatting formatting = new Formatting();

  @Setting
  public Commands commands = new Commands();

  @ConfigSerializable
  @NonNull
  public static class Errors {
    @Setting(comment = "The target player has no active ban.")
    public String noBan = "<red><gold><targetName></gold> has no active bans.";

    @Setting(comment = "The target player has no active mute.")
    public String noMute = "<red><gold><targetName></gold> has no active mutes.";
  }

  @ConfigSerializable
  @NonNull
  public static class Broadcasts {
    @Setting(comment = "A player has been banned without a reason.")
    public String banReasonless = "<yellow><gold><targetName></gold> has been banned<gold><duration></gold>.";

    @Setting(comment = "A player has been banned with a reason.")
    public String banReason = "<yellow><gold><targetName></gold> has been banned<gold><duration></gold> for: <gold><reason>";

    @Setting(comment = "A player has been unbanned.")
    public String unban = "<yellow><gold><targetName></gold> has been unbanned by <gold><punisherName></gold>.";

    @Setting(comment = "A player has been muted without reason.")
    public String muteReasonless = "<yellow><gold><targetName></gold> has been muted <gold><duration></gold>.";

    @Setting(comment = "A player has been muted with a reason.")
    public String muteReason = "<yellow><gold><targetName></gold> has been muted<gold><duration></gold> for: <gold><reason>";

    @Setting(comment = "A player has been unmuted.")
    public String unmute = "<yellow><gold><targetName></gold> has been unmuted by <gold><punisherName></gold>.";

    @Setting(comment = "A player has been kicked without reason.")
    public String kickReasonless = "<yellow><gold><targetName></gold> has been kicked.";

    @Setting(comment = "A player has been kicked with a reason.")
    public String kickReason = "<yellow><gold><targetName></gold> has been kicked for: <gold><reason>";

    @Setting(comment = "A player has been warned without reason.")
    public String warnReasonless = "<yellow><gold><targetName></gold> has been warned.";

    @Setting(comment = "A player has been warned with a reason.")
    public String warnReason = "<yellow><gold><targetName></gold> has been warned for: <gold><reason>";
  }

  @ConfigSerializable
  @NonNull
  public static class Applications {
    @Setting(comment = "Message shown to the player when they are muted without a reason.")
    public String muteReasonless = "<yellow>You have been muted by <gold><punisherName></gold>.";

    @Setting(comment = "Message shown to the player when they are muted with a reason.")
    public String muteReason = "<yellow>You have been muted by <gold><punisherName></gold> <gold><duration></gold>\n<gold><reason>";

    @Setting(comment = "Message shown to the player when they are kicked without a reason.")
    public String kickReasonless = "<yellow>You have been kicked by <gold><punisherName></gold>.";

    @Setting(comment = "Message shown to the player when they are kicked with a reason.")
    public String kickReason = "<yellow>You have been kicked by <gold><punisherName></gold>\n<gold><reason>";

    @Setting(comment = "Message shown to the player when they are banned without a reason.")
    public String banReasonless = "<yellow>You have been banned by <gold><punisherName></gold><gold><duration>.";

    @Setting(comment = "Message shown to the player when they are banned with a reason.")
    public String banReason = "<yellow>You have been banned by <gold><punisherName></gold><gold><duration></gold>\n<gold><reason>";
  }

  @ConfigSerializable
  @NonNull
  public static class Formatting {
    @Setting(comment = "The word used in the duration for when someone has been permanently punished.")
    public String permanently = " permanently";

    @Setting(comment = "The format for punishments with durations.")
    public String durationFormat = " for <duration>";

    @Setting(comment = "The word used in an expiration for when it will never expire.")
    public String never = "never";

    @Setting(comment = "The name of an expiry for when it has already been lifted.")
    public String isLifted = "lifted";

    @Setting(comment = "The past tense verb for banning.")
    public String banVerb = "banned";

    @Setting(comment = "The past tense verb for kicking.")
    public String kickVerb = "kicked";

    @Setting(comment = "The past tense verb for muting.")
    public String muteVerb = "muted";

    @Setting(comment = "The past tense verb for warning.")
    public String warnVerb = "warned";

    @Setting(comment = "The past tense verb for noting.")
    public String noteVerb = "noted";
  }

  @ConfigSerializable
  @NonNull
  public static class Commands {
    @Setting
    public String historyHeader = "<yellow>Found <gold><amount></gold> punishment(s) for <gold><targetName></gold>.";

    @Setting
    public String historyEntry = "<blue>History > <yellow><punisherName> <punishmentVerb><duration> (<expiry>): <reason>";
  }
}
