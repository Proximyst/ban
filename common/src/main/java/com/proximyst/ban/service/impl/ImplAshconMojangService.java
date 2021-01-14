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
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import com.proximyst.ban.rest.IAshconMojangApi;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IMojangService;
import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import com.proximyst.ban.utils.StringUtils;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class ImplAshconMojangService implements IMojangService {
  private static final @NonNegative int MAXIMUM_UUID_IDENTITY_CACHE_CAPACITY =
      Integer.getInteger("ban.maxBanUserCacheCapacity", 512);
  private static final @NonNegative int MAXIMUM_USERNAME_TO_UUID_CACHE_CAPACITY =
      Integer.getInteger("ban.maxUsernameToUuidCacheCapacity", 512);

  private final @NonNull IAshconMojangApi ashconMojangApi;
  private final @NonNull Executor executor;
  private final @NonNull BanExceptionalFutureLogger<?> banExceptionalFutureLogger;
  private final @NonNull IDataService dataService;

  private final @NonNull Cache<@NonNull UUID, @NonNull UuidIdentity> uuidIdentityCache = CacheBuilder.newBuilder()
      .initialCapacity(512)
      .maximumSize(MAXIMUM_UUID_IDENTITY_CACHE_CAPACITY)
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
      final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull IDataService dataService) {
    this.ashconMojangApi = ashconMojangApi;
    this.executor = executor;
    this.banExceptionalFutureLogger = banExceptionalFutureLoggerFactory.createLogger(this.getClass());
    this.dataService = dataService;
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUser(final @NonNull UUID uuid) {
    if (uuid.version() != 4) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    final UuidIdentity uuidIdentity = this.uuidIdentityCache.getIfPresent(uuid);
    if (uuidIdentity != null) {
      return CompletableFuture.completedFuture(Optional.of(uuidIdentity));
    }

    return this.fetchFromIdentifier(uuid.toString());
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> getUser(@NonNull String identifier) {
    if (identifier.length() < 3) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    if (identifier.length() > 16) {
      // Too long to be a username.
      if (identifier.length() != 32 && identifier.length() != 36) {
        return CompletableFuture.completedFuture(Optional.empty());
      }

      if (identifier.length() == 32) {
        identifier = StringUtils.rehyphenUuid(identifier);
      }

      final String finalIdentifier = identifier; // Lambda requires effectively final
      try {
        return this.getUser(UUID.fromString(finalIdentifier));
      } catch (final IllegalArgumentException ignored) {
        return CompletableFuture.completedFuture(Optional.empty());
      }
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
        .thenApply(opt -> opt.map(UuidIdentity::uuid));
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Optional<@NonNull String>> getUsername(final @NonNull UUID uuid) {
    return this.getUser(uuid)
        .thenApply(opt -> opt.map(UuidIdentity::username));
  }

  private @NonNull CompletableFuture<@NonNull Optional<@NonNull UuidIdentity>> fetchFromIdentifier(
      final @NonNull String identifier) {
    return CompletableFuture.supplyAsync(() -> this.ashconMojangApi.getUser(identifier), this.executor)
        .thenApplyAsync(opt -> {
          final Optional<UuidIdentity> user = opt.map(ashconUser ->
              this.dataService.createIdentity(ashconUser.uuid, ashconUser.username));
          user.ifPresent(ident -> {
            this.usernameUuidCache.put(ident.username(), ident.uuid());
            this.uuidIdentityCache.put(ident.uuid(), ident);
          });

          return user;
        }, this.executor)
        .exceptionally(this.banExceptionalFutureLogger.cast());
  }
}
