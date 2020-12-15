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

package com.proximyst.ban.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.service.IMojangService;
import com.proximyst.ban.utils.HttpUtils;
import com.proximyst.ban.utils.StringUtils;
import com.proximyst.ban.utils.ThrowableUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ImplAshconMojangService implements IMojangService {
  private static final @NonNull Gson GSON = new Gson();
  private static final @NonNull String API_BASE = "https://api.ashcon.app/mojang/v2/";
  private static final @NonNegative int MAXIMUM_BAN_USER_CACHE_CAPACITY =
      Integer.getInteger("ban.maxBanUserCacheCapacity", 512);
  private static final @NonNegative int MAXIMUM_USERNAME_TO_UUID_CACHE_CAPACITY =
      Integer.getInteger("ban.maxUsernameToUuidCacheCapacity", 512);
  private static final @NonNull CompletableFuture<@NonNull Optional<@NonNull BanUser>> CONSOLE_USER =
      CompletableFuture.completedFuture(Optional.of(BanUser.CONSOLE));

  private final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory;
  private final @NonNull HttpUtils httpUtils;

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
  public ImplAshconMojangService(final @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory,
      final @NonNull HttpUtils httpUtils) {
    this.banExceptionalFutureLoggerFactory = banExceptionalFutureLoggerFactory;
    this.httpUtils = httpUtils;
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
    return this.httpUtils.get(API_BASE + "user/" + identifier)
        .thenApply(opt -> {
          final Optional<BanUser> user = opt.map(json -> GSON.fromJson(json, AshconUser.class).toBanUser());
          user.ifPresentOrElse(banUser -> this.usernameUuidCache.put(banUser.getUsername(), banUser.getUuid()),
              () -> {
                throw new IllegalArgumentException("No user \"" + identifier + "\" exists.");
              });

          return user;
        });
  }

  @NonNull
  static class AshconUser {
    @SerializedName("uuid")
    UUID uuid;

    @SerializedName("username")
    String username;

    @SerializedName("username_history")
    @Nullable List<UsernameHistory.@NonNull Entry> history;

    @NonNull BanUser toBanUser() {
      return new BanUser(this.uuid,
          this.username,
          new UsernameHistory(this.uuid,
              this.history == null
                  ? Collections.singleton(new UsernameHistory.Entry(this.username, null))
                  : this.history));
    }
  }
}
