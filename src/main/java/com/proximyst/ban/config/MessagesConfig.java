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

  @ConfigSerializable
  @NonNull
  public static class Errors {
    @Setting(comment = "The target player has no active ban.")
    public String noBan = "<red><gold><targetName></gold> has no active bans.";
  }

  @ConfigSerializable
  @NonNull
  public static class Broadcasts {
    @Setting(comment = "A player has been banned without a reason.")
    public String banReasonless = "<yellow><gold><targetName></gold> has been banned<gold><duration></gold>.";

    @Setting(comment = "A player has been banned with a reason.")
    public String banReason = "<yellow><gold><targetName></gold> has been banned<gold><duration></gold> for:<gold><reason>";

    @Setting(comment = "A player has been unbanned.")
    public String unban = "<yellow><gold><targetName></gold> has been unbanned by <gold><punisherName></gold>.";

    @Setting(comment = "A player has been muted without reason.")
    public String muteReasonless = "<yellow><gold><targetName></gold> has been muted <gold><duration></gold>.";

    @Setting(comment = "A player has been muted with a reason.")
    public String muteReason = "<yellow><gold><targetName></gold> has been muted<gold><duration></gold> for: <gold><reason>";

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
    public String permanently = "permanently";

    @Setting(comment = "The format for punishments with durations.")
    public String durationFormat = " for <duration>";
  }
}
