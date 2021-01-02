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

package com.proximyst.ban.config;

import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

// TODO(Proximyst): Document all this
public enum MessageKey {
  /**
   * The supplied target has no active ban.
   */
  ERROR_NO_ACTIVE_BAN(cfg -> cfg.errors.noBan),

  /**
   * The supplied target has no active mute.
   */
  ERROR_NO_ACTIVE_MUTE(cfg -> cfg.errors.noMute),

  /**
   * A player has been banned without a reason.
   */
  BROADCAST_REASONLESS_BAN(cfg -> cfg.broadcasts.banReasonless),

  /**
   * A player has been banned with a reason.
   */
  BROADCAST_REASONED_BAN(cfg -> cfg.broadcasts.banReason),

  BROADCAST_REASONLESS_KICK(cfg -> cfg.broadcasts.kickReasonless),
  BROADCAST_REASONED_KICK(cfg -> cfg.broadcasts.kickReason),

  BROADCAST_REASONLESS_WARN(cfg -> cfg.broadcasts.warnReasonless),
  BROADCAST_REASONED_WARN(cfg -> cfg.broadcasts.warnReason),

  BROADCAST_REASONLESS_MUTE(cfg -> cfg.broadcasts.muteReasonless),
  BROADCAST_REASONED_MUTE(cfg -> cfg.broadcasts.muteReason),

  BROADCAST_UNBAN(cfg -> cfg.broadcasts.unban),
  BROADCAST_UNMUTE(cfg -> cfg.broadcasts.unmute),

  APPLICATION_REASONLESS_MUTE(cfg -> cfg.applications.muteReasonless),
  APPLICATION_REASONED_MUTE(cfg -> cfg.applications.muteReason),
  APPLICATION_REASONLESS_KICK(cfg -> cfg.applications.kickReasonless),
  APPLICATION_REASONED_KICK(cfg -> cfg.applications.kickReason),
  APPLICATION_REASONLESS_BAN(cfg -> cfg.applications.banReasonless),
  APPLICATION_REASONED_BAN(cfg -> cfg.applications.banReason),

  FORMATTING_PERMANENTLY(cfg -> cfg.formatting.permanently),
  FORMATTING_DURATION(cfg -> cfg.formatting.durationFormat),
  FORMATTING_NEVER(cfg -> cfg.formatting.never),
  FORMATTING_LIFTED(cfg -> cfg.formatting.isLifted),
  FORMATTING_VERB_PAST_BAN(cfg -> cfg.formatting.banVerb),
  FORMATTING_VERB_PAST_KICK(cfg -> cfg.formatting.kickVerb),
  FORMATTING_VERB_PAST_MUTE(cfg -> cfg.formatting.muteVerb),
  FORMATTING_VERB_PAST_WARN(cfg -> cfg.formatting.warnVerb),
  FORMATTING_VERB_PAST_NOTE(cfg -> cfg.formatting.noteVerb),

  COMMANDS_FEEDBACK_BAN(cfg -> cfg.commands.banFeedback),
  COMMANDS_FEEDBACK_KICK(cfg -> cfg.commands.kickFeedback),
  COMMANDS_FEEDBACK_HISTORY(cfg -> cfg.commands.historyFeedback),
  COMMANDS_FEEDBACK_MUTE(cfg -> cfg.commands.muteFeedback),
  COMMANDS_FEEDBACK_UNMUTE(cfg -> cfg.commands.unmuteFeedback),
  COMMANDS_FEEDBACK_UNBAN(cfg -> cfg.commands.unbanFeedback),
  COMMANDS_HISTORY_HEADER(cfg -> cfg.commands.historyHeader),
  COMMANDS_HISTORY_ENTRY(cfg -> cfg.commands.historyEntry),
  ;

  private final @NonNull Function<@NonNull MessagesConfig, @NonNull String> mapper;

  MessageKey(final @NonNull Function<@NonNull MessagesConfig, @NonNull String> mapper) {
    this.mapper = mapper;
  }

  public @NonNull String map(final @NonNull MessagesConfig config) {
    return this.mapper.apply(config);
  }
}
