//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
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

package com.proximyst.ban.service;

import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.message.ServerReceiver;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.moonshine.annotation.Message;
import com.proximyst.moonshine.annotation.Placeholder;
import com.proximyst.moonshine.annotation.Receiver;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface IMessageService {
  @Message("error.no-active-ban")
  void errorNoActiveBan(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("error.no-active-mute")
  void errorNoActiveMute(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("commands.feedback.ban")
  void feedbackBan(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("commands.feedback.mute")
  void feedbackMute(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("commands.feedback.kick")
  void feedbackKick(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("commands.feedback.history")
  void feedbackHistory(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("commands.feedback.unban")
  void feedbackUnban(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("commands.feedback.unmute")
  void feedbackUnmute(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target);

  @Message("commands.feedback.history.header")
  void feedbackHistoryHeader(@Receiver final IBanAudience audience,
      @Placeholder final BanIdentity target,
      @Placeholder final int amount);

  @Message("commands.feedback.history.entry")
  void feedbackHistoryEntry(@Receiver final IBanAudience audience,
      @Placeholder final Punishment punishment);

  @Message("broadcasts.reasonless.ban")
  @ServerReceiver(permission = BanPermissions.NOTIFY_BAN)
  void broadcastsReasonlessBan(@Placeholder final Punishment punishment);

  @Message("broadcasts.reasoned.ban")
  @ServerReceiver(permission = BanPermissions.NOTIFY_BAN)
  void broadcastsReasonedBan(@Placeholder final Punishment punishment);

  @Message("broadcasts.reasonless.mute")
  @ServerReceiver(permission = BanPermissions.NOTIFY_MUTE)
  void broadcastsReasonlessMute(@Placeholder final Punishment punishment);

  @Message("broadcasts.reasoned.mute")
  @ServerReceiver(permission = BanPermissions.NOTIFY_MUTE)
  void broadcastsReasonedMute(@Placeholder final Punishment punishment);

  @Message("broadcasts.reasonless.kick")
  @ServerReceiver(permission = BanPermissions.NOTIFY_KICK)
  void broadcastsReasonlessKick(@Placeholder final Punishment punishment);

  @Message("broadcasts.reasoned.kick")
  @ServerReceiver(permission = BanPermissions.NOTIFY_KICK)
  void broadcastsReasonedKick(@Placeholder final Punishment punishment);

  @Message("broadcasts.reasonless.warn")
  @ServerReceiver(permission = BanPermissions.NOTIFY_WARN)
  void broadcastsReasonlessWarn(@Placeholder final Punishment punishment);

  @Message("broadcasts.reasoned.warn")
  @ServerReceiver(permission = BanPermissions.NOTIFY_WARN)
  void broadcastsReasonedWarn(@Placeholder final Punishment punishment);

  @Message("broadcasts.unban")
  @ServerReceiver(permission = BanPermissions.NOTIFY_BAN)
  void broadcastsUnban(@Placeholder final Punishment punishment);

  @Message("broadcasts.unmute")
  @ServerReceiver(permission = BanPermissions.NOTIFY_MUTE)
  void broadcastsUnmute(@Placeholder final Punishment punishment);

  @Message("applications.reasonless.ban")
  @NonNull Component applicationsReasonlessBan(@Placeholder final Punishment punishment);

  @Message("applications.reasonless.mute")
  void applicationsReasonlessMute(@Receiver final IBanAudience audience,
      @Placeholder final Punishment punishment);

  @Message("applications.reasonless.kick")
  @NonNull Component applicationsReasonlessKick(@Placeholder final Punishment punishment);

  @Message("applications.reasonless.warn")
  void applicationsReasonlessWarn(@Receiver final IBanAudience audience,
      @Placeholder final Punishment punishment);

  @Message("applications.reasoned.ban")
  @NonNull Component applicationsReasonedBan(@Placeholder final Punishment punishment);

  @Message("applications.reasoned.mute")
  void applicationsReasonedMute(@Receiver final @NonNull IBanAudience audience,
      @Placeholder final Punishment punishment);

  @Message("applications.reasoned.kick")
  @NonNull Component applicationsReasonedKick(@Placeholder final Punishment punishment);

  @Message("applications.reasoned.warn")
  void applicationsReasonedWarn(@Receiver final IBanAudience audience,
      @Placeholder final Punishment punishment);

  @Message("formatting.permanently")
  @NonNull Component formattingPermanently();

  @Message("formatting.never")
  @NonNull Component formattingNever();

  @Message("formatting.duration")
  @NonNull Component formattingDuration(@Placeholder final String duration);

  @Message("formatting.lifted")
  @NonNull Component formattingLifted();

  @Message("formatting.verbs.past.ban")
  @NonNull Component formattingVerbsPastBan();

  @Message("formatting.verbs.past.kick")
  @NonNull Component formattingVerbsPastKick();

  @Message("formatting.verbs.past.mute")
  @NonNull Component formattingVerbsPastMute();

  @Message("formatting.verbs.past.warn")
  @NonNull Component formattingVerbsPastWarn();

  @Message("formatting.verbs.past.note")
  @NonNull Component formattingVerbsPastNote();
}
