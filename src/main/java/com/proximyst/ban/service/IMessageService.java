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

import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IMessageService {
  @NonNull CompletableFuture<@Nullable Void> announceNewPunishment(final @NonNull Punishment punishment);

  @NonNull CompletableFuture<@Nullable Void> announceLiftedPunishment(final @NonNull Punishment punishment);

  @NonNull Component errorNoBan(final @NonNull BanUser user);

  @NonNull Component errorNoMute(final @NonNull BanUser user);

  @NonNull CompletableFuture<@NonNull Component> formatMessageWith(
      final @NonNull String message,
      final @NonNull Punishment punishment);

  @NonNull Component formatMessageWith(
      final @NonNull Punishment punishment,
      final @NonNull String message,
      final @NonNull BanUser punisher,
      final @NonNull BanUser target);
}
