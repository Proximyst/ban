package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class MessagesConfig {
  @Setting(comment = "A player has been punished silently.")
  private String silentPrefix = "<gray>[Silent] </gray>";

  @Setting(comment = "A player has been banned without reason.")
  private String broadcastBanReasonless = "<yellow><gold><name></gold> has been banned <gold><duration></gold>.";

  @Setting(comment = "A player has been banned with a reason.")
  private String broadcastBanReason = "<yellow><gold><name></gold> has been banned <gold><duration></gold> for: <gold><reason>";

  @Setting(comment = "A player has been muted without reason.")
  private String broadcastMuteReasonless = "<yellow><gold><name></gold> has been muted <gold><duration></gold>.";

  @Setting(comment = "A player has been muted with a reason.")
  private String broadcastMuteReason = "<yellow><gold><name></gold> has been muted <gold><duration></gold> for: <gold><reason>";

  @Setting(comment = "A player has been kicked without reason.")
  private String broadcastKickReasonless = "<yellow><gold><name></gold> has been kicked.";

  @Setting(comment = "A player has been kicked with a reason.")
  private String broadcastKickReason = "<yellow><gold><name></gold> has been kicked for: <gold><reason>";

  @Setting(comment = "A player has been warned without reason.")
  private String broadcastWarnReasonless = "<yellow><gold><name></gold> has been warned.";

  @Setting(comment = "A player has been warned with a reason.")
  private String broadcastWarnReason = "<yellow><gold><name></gold> has been warned for: <gold><reason>";

  @Setting(comment = "The word used in the duration for when someone has been permanently punished.")
  private String permanently = "permanently";

  @Setting(comment = "The format for punishments with durations.")
  public String durationFormat = "for <duration>";

  @Setting(comment = "Message shown to the player when they are muted without a reason.")
  private String muteMessageReasonless = "<yellow>You have been muted by <gold><punisher></gold>";

  @Setting(comment = "Message shown to the player when they are muted with a reason.")
  private String muteMessageReason = "<yellow>You have been muted by <gold><punisher></gold> <gold><duration></gold> for:\n<gold><reason>";

  @Setting(comment = "Message shown to the player when they are kicked without a reason.")
  private String kickMessageReasonless = "<yellow>You have been kicked by <gold><punisher></gold>";

  @Setting(comment = "Message shown to the player when they are kicked with a reason.")
  private String kickMessageReason = "<yellow>You have been kicked by <gold><punisher></gold> for:\n<gold><reason>";

  @Setting(comment = "Message shown to the player when they are banned without a reason.")
  private String banMessageReasonless = "<yellow>You have been banned by <gold><punisher></gold> <gold><duration>";

  @Setting(comment = "Message shown to the player when they are banned with a reason.")
  private String banMessageReason = "<yellow>You have been banned by <gold><punisher></gold> <gold><duration></gold> for:\n<gold><reason>";

  @NonNull
  public String getSilentPrefix() {
    return silentPrefix;
  }

  @NonNull
  public String getBroadcastBanReasonless() {
    return broadcastBanReasonless;
  }

  @NonNull
  public String getBroadcastBanReason() {
    return broadcastBanReason;
  }

  @NonNull
  public String getBroadcastMuteReasonless() {
    return broadcastMuteReasonless;
  }

  @NonNull
  public String getBroadcastMuteReason() {
    return broadcastMuteReason;
  }

  @NonNull
  public String getBroadcastKickReasonless() {
    return broadcastKickReasonless;
  }

  @NonNull
  public String getBroadcastKickReason() {
    return broadcastKickReason;
  }

  @NonNull
  public String getBroadcastWarnReasonless() {
    return broadcastWarnReasonless;
  }

  @NonNull
  public String getBroadcastWarnReason() {
    return broadcastWarnReason;
  }

  @NonNull
  public String getPermanently() {
    return permanently;
  }

  @NonNull
  public String getDurationFormat() {
    return durationFormat;
  }

  @NonNull
  public String getKickMessageReasonless() {
    return kickMessageReasonless;
  }

  @NonNull
  public String getKickMessageReason() {
    return kickMessageReason;
  }

  @NonNull
  public String getBanMessageReasonless() {
    return banMessageReasonless;
  }

  @NonNull
  public String getBanMessageReason() {
    return banMessageReason;
  }

  @NonNull
  public String getMuteMessageReasonless() {
    return muteMessageReasonless;
  }

  @NonNull
  public String getMuteMessageReason() {
    return muteMessageReason;
  }
}
