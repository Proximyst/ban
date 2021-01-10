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

package com.proximyst.ban.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.rest.IAshconMojangApi;
import com.proximyst.ban.service.IMojangService;
import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import com.proximyst.ban.utils.StringUtils;
import com.proximyst.ban.utils.ThrowableUtils;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class ImplAshconMojangService implements IMojangService {
  private static final @NonNegative int MAXIMUM_BAN_USER_CACHE_CAPACITY =
      Integer.getInteger("ban.maxBanUserCacheCapacity", 512);
  private static final @NonNegative int MAXIMUM_USERNAME_TO_UUID_CACHE_CAPACITY =
      Integer.getInteger("ban.maxUsernameToUuidCacheCapacity", 512);
  private static final @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> CONSOLE_USER =
      CompletableFuture.completedFuture(Optional.of(BanUser.CONSOLE));

  private final @NonNull IAshconMojangApi ashconMojangApi;
  private final @NonNull Executor executor;
  private final @NonNull BanExceptionalFutureLogger<?> banExceptionalFutureLogger;

  private final @NonNull Cache<@NonNull UUID, @NonNull BanUser> banUserCache = CacheBuilder.newBuilder()
      .initialCapacity(512)
      .maximumSize(MAXIMUM_BAN_USER_CACHE_CAPACITY)
      .expireAfterWrite(2, TimeUnit.MINUTES)
      .build();

  private final @NonNull Cache<@NonNull String, @NonNull UUID> usernameUuidCache = CacheBuilder.newBuilder()
      .initialCapacity(512)
      .maximumSize(MAXIMUM_USERNAME_TO_UUID_CACHE_CAPACITY)
      .expireAfterWrite(2, TimeUnit.MINUTES)
      .build();

  @Inject
  ImplAshconMojangService(final @NonNull IAshconMojangApi ashconMojangApi,
      final @NonNull @BanAsyncExecutor Executor executor,
      final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory) {
    this.ashconMojangApi = ashconMojangApi;
    this.executor = executor;
    this.banExceptionalFutureLogger = banExceptionalFutureLoggerFactory.createLogger(this.getClass());
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(final @NonNull UUID uuid) {
    if (uuid.equals(BanUser.CONSOLE.getUuid())) {
      return CONSOLE_USER;
    }

    if (uuid.version() != 4) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("UUID \"" + uuid + "\" is not an online-mode UUID"));
    }

    final BanUser cachedUser = this.banUserCache.getIfPresent(uuid);
    if (cachedUser != null) {
      return CompletableFuture.completedFuture(Optional.of(cachedUser));
    }

    return this.fetchFromIdentifier(uuid.toString());
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(@NonNull String identifier) {
    if (identifier.length() < 3) {
      // Too short to be a username.
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Username \"" + identifier + "\" is too short."));
    }

    if (identifier.length() > 16) {
      // Too long to be a username.
      if (identifier.length() != 32 && identifier.length() != 36) {
        return CompletableFuture.failedFuture(
            new IllegalArgumentException("Username/UUID \"" + identifier + "\" has an invalid length."));
      }

      if (identifier.length() == 32) {
        identifier = StringUtils.rehyphenUuid(identifier);
      }

      final String finalIdentifier = identifier; // Lambda requires effectively final
      return ThrowableUtils.supplySneaky(() -> UUID.fromString(finalIdentifier))
          .thenCompose(this::getUser);
    }

    // This is a name. Let's check if they're already cached.
    final UUID uuid = this.usernameUuidCache.getIfPresent(identifier);
    if (uuid != null) {
      return this.getUser(uuid);
    }

    return this.fetchFromIdentifier(identifier);
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull UUID>> getUuid(final @NonNull String identifier) {
    return this.getUser(identifier)
        .thenApply(opt -> opt.map(BanUser::getUuid));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull String>> getUsername(final @NonNull UUID uuid) {
    return this.getUser(uuid)
        .thenApply(opt -> opt.map(BanUser::getUsername));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull UsernameHistory>> getUsernameHistory(
      final @NonNull UUID uuid) {
    return this.getUser(uuid)
        .thenApply(opt -> opt.map(BanUser::getUsernameHistory));
  }

  private @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> fetchFromIdentifier(
      final @NonNull String identifier) {
    return CompletableFuture.supplyAsync(() -> this.ashconMojangApi.getUser(identifier), this.executor)
        .thenApply(opt -> {
          final Optional<BanUser> user = opt.map(IAshconMojangApi.AshconUser::toBanUser);
          user.ifPresent(banUser -> this.usernameUuidCache.put(banUser.getUsername(), banUser.getUuid()));

          return user;
        })
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }
}
