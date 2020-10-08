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

package com.proximyst.ban.manager;

import com.google.inject.Singleton;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.model.BanUser;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class UserManager {
  private final @NonNull BanPlugin main;

  public UserManager(final @NonNull BanPlugin main) {
    this.main = main;
  }

  @SuppressWarnings("DuplicatedCode")
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(final @NonNull UUID uuid) {
    if (uuid == BanUser.CONSOLE.getUuid()) {
      return CompletableFuture.completedFuture(Optional.of(BanUser.CONSOLE));
    }

    final CompletableFuture<Optional<@NonNull BanUser>> future = CompletableFuture
        .supplyAsync(() -> this.main.getDataInterface().getUser(uuid), this.main.getSchedulerExecutor());
    return future.thenCompose(user -> {
      user.map(BanUser::getUuid).ifPresent(this::scheduleUpdateIfNecessary);
      if (!user.isPresent()) {
        return this.main.getMojangApi().getUser(uuid).getOrLoad()
            .thenApply(optionalUser -> {
              optionalUser.ifPresent(fetchedUser ->
                  this.main.getProxyServer().getScheduler()
                      .buildTask(this.main, () -> this.main.getDataInterface().saveUser(fetchedUser))
                      .schedule()
              );
              return optionalUser;
            });
      }

      return future;
    });
  }

  @SuppressWarnings("DuplicatedCode")
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(final @NonNull String name) {
    final CompletableFuture<Optional<@NonNull BanUser>> future = CompletableFuture
        .supplyAsync(() -> this.main.getDataInterface().getUser(name), this.main.getSchedulerExecutor());
    return future.thenCompose(user -> {
      user.map(BanUser::getUuid).ifPresent(this::scheduleUpdateIfNecessary);
      if (!user.isPresent()) {
        return this.main.getMojangApi().getUser(name).getOrLoad()
            .thenApply(optionalUser -> {
              optionalUser.ifPresent(fetchedUser ->
                  this.main.getProxyServer().getScheduler()
                      .buildTask(this.main, () -> this.main.getDataInterface().saveUser(fetchedUser))
                      .schedule()
              );
              return optionalUser;
            });
      }

      return future;
    });
  }

  public void scheduleUpdateIfNecessary(final @NonNull UUID uuid) {
    this.main.getProxyServer().getScheduler()
        .buildTask(this.main, () -> {
          final long lastUpdate = this.main.getDataInterface().getUserCacheDate(uuid).orElse(0L);
          if (lastUpdate + TimeUnit.DAYS.toMillis(1) <= System.currentTimeMillis()) {
            this.updateUser(uuid);
          }
        })
        .schedule();
  }

  public @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> updateUser(final @NonNull UUID identifier) {
    return this.main.getMojangApi().getUser(identifier).getOrLoad()
        .thenApply(optionalUser -> {
          optionalUser.ifPresent(fetchedUser -> {
            this.main.getProxyServer().getScheduler()
                .buildTask(this.main, () -> this.main.getDataInterface().saveUser(fetchedUser))
                .schedule();
          });
          return optionalUser;
        });
  }
}
