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

package com.proximyst.ban.service;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.proximyst.ban.config.MessageKey;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A service that handles creation of and sending of messages to players.
 * <p>
 * Some of these methods may not be threaded and will just execute its logic in a blocking manner before returning a
 * {@link CompletableFuture#isDone() completed future}.
 */
public interface IMessageService {
  /**
   * Announces a newly placed {@link Punishment} to all players who are eligible to see the event, as well as console.
   *
   * @param punishment The punishment to announce the addition of.
   * @return A {@link CompletableFuture} which indicates when the announcement has been sent.
   */
  @NonNull CompletableFuture<@Nullable Void> announceNewPunishment(final @NonNull Punishment punishment);

  /**
   * Announces a newly lifted {@link Punishment} to all players who are eligible to see the event, as well as console.
   *
   * @param punishment The punishment to announce the removal of.
   * @return A {@link CompletableFuture} which indicates when the announcement has been sent.
   */
  @NonNull CompletableFuture<@Nullable Void> announceLiftedPunishment(final @NonNull Punishment punishment);

  /**
   * Formats the punishment history of a {@link BanUser}.
   *
   * @param punishments The punishments the {@link BanUser} has.
   * @param target      The {@link BanUser} whose history is being formatted.
   * @return The entire formatted history of the {@code target}. If all the data of the punishments is already loaded,
   * the future will be complete pretty much instantly.
   */
  @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Component>> formatHistory(
      final @NonNull ImmutableCollection<@NonNull Punishment> punishments,
      final @NonNull BanUser target);

  /**
   * Format a {@link MessageKey} with given placeholders.
   * <p>
   * A placeholder may be a {@link CompletableFuture}, in which case the method will return a new {@link
   * CompletableFuture} which depends on the completion of the placeholder {@link CompletableFuture future}(s).
   * <p>
   * {@link CompletableFuture}s inside other {@link CompletableFuture}s will not be awaited; passing objects of this
   * type may result in unexpected although defined behaviour.
   *
   * @param messageKey   The message to format.
   * @param placeholders The placeholders to use. The {@code length} of this must fulfill {@code length % 2 == 0}.
   * @return A {@link CompletableFuture} of a {@link Component}. If there are no futures in the {@code placeholders},
   * the future will be completed. If there is a future in the {@code placeholders}, the future will only be completed
   * once all the futures in {@code placeholders} are completed.
   */
  @NonNull CompletableFuture<Component> formatMessage(
      final @Nullable MessageKey messageKey,
      final @Nullable Object @NonNull ... placeholders);

  /**
   * Format a {@link MessageKey} with given placeholders with provided placeholders for the provided {@link
   * Punishment}.
   * <p>
   * A placeholder may be a {@link CompletableFuture}, in which case the method will return a new {@link
   * CompletableFuture} which depends on the completion of the placeholder {@link CompletableFuture future}(s).
   * <p>
   * {@link CompletableFuture}s inside other {@link CompletableFuture}s will not be awaited; passing objects of this
   * type may result in unexpected although defined behaviour.
   *
   * @param messageKey   The message to format.
   * @param punishment   The punishment to create pre-filled placeholders for.
   * @param placeholders The placeholders to use. The {@code length} of this must fulfill {@code length % 2 == 0}.
   * @return A {@link CompletableFuture} of a {@link Component}. If there are no futures in the {@code placeholders},
   * the future will be completed. If there is a future in the {@code placeholders}, the future will only be completed
   * once all the futures in {@code placeholders} are completed.
   */
  @NonNull CompletableFuture<Component> formatMessage(final @Nullable MessageKey messageKey,
      final @NonNull Punishment punishment,
      final @Nullable Object @NonNull ... placeholders);

  /**
   * Format a {@link MessageKey} with given placeholders, then send it to the given {@link Audience}.
   * <p>
   * A placeholder may be a {@link CompletableFuture}, in which case the method will return a new {@link
   * CompletableFuture} which depends on the completion of the placeholder {@link CompletableFuture future}(s).
   * <p>
   * {@link CompletableFuture}s inside other {@link CompletableFuture}s will not be awaited; passing objects of this
   * type may result in unexpected although defined behaviour.
   * <p>
   * This will not send empty messages. If a message is however only a space, it will still be sent.
   *
   * @param audience     The audience to receive the formatted message.
   * @param identity     The identity of the message's sender.
   * @param messageKey   The message to format.
   * @param placeholders The placeholders to use. The {@code length} of this must fulfill {@code length % 2 == 0}.
   * @return A {@link CompletableFuture} indicating its completion state. If there are no futures in the {@code
   * placeholders}, the future will be completed. If there is a future in the {@code placeholders}, the future will only
   * be completed once all the futures in {@code placeholders} are completed.
   * @see #formatMessage(MessageKey, Object...)
   */
  default @NonNull CompletableFuture<Void> sendFormattedMessage(final @NonNull Audience audience,
      final @NonNull Identity identity,
      final @Nullable MessageKey messageKey,
      final @Nullable Object @NonNull ... placeholders) {
    return this.formatMessage(messageKey, placeholders)
        .thenAccept(component -> {
          if (component != Component.empty()) {
            audience.sendMessage(identity, component);
          }
        });
  }

  /**
   * Format a {@link MessageKey} with given placeholders, then send it to the given {@link Audience}.
   * <p>
   * A placeholder may be a {@link CompletableFuture}, in which case the method will return a new {@link
   * CompletableFuture} which depends on the completion of the placeholder {@link CompletableFuture future}(s).
   * <p>
   * {@link CompletableFuture}s inside other {@link CompletableFuture}s will not be awaited; passing objects of this
   * type may result in unexpected although defined behaviour.
   * <p>
   * This will not send empty messages. If a message is however only a space, it will still be sent.
   *
   * @param audience     The audience to receive the formatted message.
   * @param identity     The identity of the message's sender.
   * @param messageKey   The message to format.
   * @param placeholders The placeholders to use. The {@code length} of this must fulfill {@code length % 2 == 0}.
   * @return A {@link CompletableFuture} indicating its completion state. If there are no futures in the {@code
   * placeholders}, the future will be completed. If there is a future in the {@code placeholders}, the future will only
   * be completed once all the futures in {@code placeholders} are completed.
   * @see #sendFormattedMessage(Audience, Identity, MessageKey, Object...)
   * @see #formatMessage(MessageKey, Object...)
   */
  default @NonNull CompletableFuture<Void> sendFormattedMessage(final @NonNull Audience audience,
      final @NonNull Identified identity,
      final @Nullable MessageKey messageKey,
      final @Nullable Object @NonNull ... placeholders) {
    return this.sendFormattedMessage(audience, identity.identity(), messageKey, placeholders);
  }

  /**
   * Format a {@link MessageKey} with given placeholders and provided placeholders for the given {@link Punishment},
   * then send it to the given {@link Audience}.
   * <p>
   * A placeholder may be a {@link CompletableFuture}, in which case the method will return a new {@link
   * CompletableFuture} which depends on the completion of the placeholder {@link CompletableFuture future}(s).
   * <p>
   * {@link CompletableFuture}s inside other {@link CompletableFuture}s will not be awaited; passing objects of this
   * type may result in unexpected although defined behaviour.
   * <p>
   * This will not send empty messages. If a message is however only a space, it will still be sent.
   *
   * @param audience     The audience to receive the formatted message.
   * @param identity     The identity of the message's sender.
   * @param messageKey   The message to format.
   * @param placeholders The placeholders to use. The {@code length} of this must fulfill {@code length % 2 == 0}.
   * @return A {@link CompletableFuture} indicating its completion state. If there are no futures in the {@code
   * placeholders}, the future will be completed. If there is a future in the {@code placeholders}, the future will only
   * be completed once all the futures in {@code placeholders} are completed.
   * @see #formatMessage(MessageKey, Punishment, Object...)
   */
  default @NonNull CompletableFuture<Void> sendFormattedMessage(final @NonNull Audience audience,
      final @NonNull Identity identity,
      final @Nullable MessageKey messageKey,
      final @NonNull Punishment punishment,
      final @Nullable Object @NonNull ... placeholders) {
    return this.formatMessage(messageKey, punishment, placeholders)
        .thenAccept(component -> {
          if (component != Component.empty()) {
            audience.sendMessage(identity, component);
          }
        });
  }

  /**
   * Format a {@link MessageKey} with given placeholders and provided placeholders for the given {@link Punishment},
   * then send it to the given {@link Audience}.
   * <p>
   * A placeholder may be a {@link CompletableFuture}, in which case the method will return a new {@link
   * CompletableFuture} which depends on the completion of the placeholder {@link CompletableFuture future}(s).
   * <p>
   * {@link CompletableFuture}s inside other {@link CompletableFuture}s will not be awaited; passing objects of this
   * type may result in unexpected although defined behaviour.
   * <p>
   * This will not send empty messages. If a message is however only a space, it will still be sent.
   *
   * @param audience     The audience to receive the formatted message.
   * @param identity     The identity of the message's sender.
   * @param messageKey   The message to format.
   * @param placeholders The placeholders to use. The {@code length} of this must fulfill {@code length % 2 == 0}.
   * @return A {@link CompletableFuture} indicating its completion state. If there are no futures in the {@code
   * placeholders}, the future will be completed. If there is a future in the {@code placeholders}, the future will only
   * be completed once all the futures in {@code placeholders} are completed.
   * @see #sendFormattedMessage(Audience, Identity, MessageKey, Punishment, Object...)
   * @see #formatMessage(MessageKey, Punishment, Object...)
   */
  default @NonNull CompletableFuture<Void> sendFormattedMessage(final @NonNull Audience audience,
      final @NonNull Identified identity,
      final @Nullable MessageKey messageKey,
      final @NonNull Punishment punishment,
      final @Nullable Object @NonNull ... placeholders) {
    return this.sendFormattedMessage(audience, identity.identity(), messageKey, punishment, placeholders);
  }
}
