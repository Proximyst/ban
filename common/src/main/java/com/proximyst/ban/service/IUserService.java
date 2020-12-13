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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

// TODO(Proximyst): Document this
public interface IUserService {
  /**
   * Save the given user.
   *
   * @param user The user to update the database with.
   * @see IDataService#saveUser(BanUser)
   */
  @NonNull CompletableFuture<@Nullable Void> saveUser(final @NonNull BanUser user);

  @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(final @NonNull String name);

  @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(final @NonNull UUID uuid);

  @NonNull CompletableFuture<@NonNull Boolean> scheduleUpdateIfNecessary(final @NonNull UUID uuid);

  @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUserUpdated(final @NonNull UUID uuid);
}
