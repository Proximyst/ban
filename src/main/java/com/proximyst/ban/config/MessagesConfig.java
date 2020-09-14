package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class MessagesConfig {
  @Setting(comment = "The target player has no active ban.")
  public String errorNoBan = "<red><gold><name></gold> has no active bans.";

  @Setting(comment = "A player has been banned without a reason.")
  public String broadcastBanReasonless = "<yellow><gold><name></gold> has been banned <gold><duration></gold>.";

  @Setting(comment = "A player has been banned with a reason.")
  public String broadcastBanReason = "<yellow><gold><name></gold> has been banned <gold><duration></gold>for: <gold><reason>";

  @Setting(comment = "A player has been unbanned.")
  public String broadcastUnban = "<yellow><gold><name></gold> has been unbanned by <gold><punisher></gold>.";

  @Setting(comment = "A player has been muted without reason.")
  public String broadcastMuteReasonless = "<yellow><gold><name></gold> has been muted <gold><duration></gold>.";

  @Setting(comment = "A player has been muted with a reason.")
  public String broadcastMuteReason = "<yellow><gold><name></gold> has been muted <gold><duration></gold>for: <gold><reason>";

  @Setting(comment = "A player has been kicked without reason.")
  public String broadcastKickReasonless = "<yellow><gold><name></gold> has been kicked.";

  @Setting(comment = "A player has been kicked with a reason.")
  public String broadcastKickReason = "<yellow><gold><name></gold> has been kicked for: <gold><reason>";

  @Setting(comment = "A player has been warned without reason.")
  public String broadcastWarnReasonless = "<yellow><gold><name></gold> has been warned.";

  @Setting(comment = "A player has been warned with a reason.")
  public String broadcastWarnReason = "<yellow><gold><name></gold> has been warned for: <gold><reason>";

  @Setting(comment = "The word used in the duration for when someone has been permanently punished.")
  public String permanently = "permanently";

  @Setting(comment = "The format for punishments with durations.")
  public String durationFormat = " for <duration>";

  @Setting(comment = "Message shown to the player when they are muted without a reason.")
  public String muteMessageReasonless = "<yellow>You have been muted by <gold><punisher></gold>.";

  @Setting(comment = "Message shown to the player when they are muted with a reason.")
  public String muteMessageReason = "<yellow>You have been muted by <gold><punisher></gold> <gold><duration></gold>for:\n<gold><reason>";

  @Setting(comment = "Message shown to the player when they are kicked without a reason.")
  public String kickMessageReasonless = "<yellow>You have been kicked by <gold><punisher></gold>.";

  @Setting(comment = "Message shown to the player when they are kicked with a reason.")
  public String kickMessageReason = "<yellow>You have been kicked by <gold><punisher></gold> for:\n<gold><reason>";

  @Setting(comment = "Message shown to the player when they are banned without a reason.")
  public String banMessageReasonless = "<yellow>You have been banned by <gold><punisher></gold> <gold><duration>.";

  @Setting(comment = "Message shown to the player when they are banned with a reason.")
  public String banMessageReason = "<yellow>You have been banned by <gold><punisher></gold> <gold><duration></gold>for:\n<gold><reason>";
}
