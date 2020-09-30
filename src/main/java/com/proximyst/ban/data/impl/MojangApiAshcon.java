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

package com.proximyst.ban.data.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.annotations.SerializedName;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.data.IMojangApi;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.utils.HttpUtils;
import com.proximyst.ban.utils.StringUtils;
import com.proximyst.sewer.SewerSystem;
import com.proximyst.sewer.loadable.Loadable;
import com.proximyst.sewer.piping.ImmediatePipeHandler;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MojangApiAshcon implements IMojangApi {
  @NonNull
  private static final String API_BASE = "https://api.ashcon.app/mojang/v2/";

  @NonNull
  private final Executor velocityExecutor;

  @NonNull
  private final Cache<UUID, BanUser> banUserCache = CacheBuilder.newBuilder()
      .maximumSize(512)
      .initialCapacity(512)
      .expireAfterWrite(1, TimeUnit.MINUTES) // We're not too scared of requests with Ashcon.
      .build();

  @NonNull
  private final Cache<String, UUID> usernameUuidCache = CacheBuilder.newBuilder()
      .maximumSize(512)
      .initialCapacity(512)
      .expireAfterWrite(1, TimeUnit.MINUTES) // We're not too scared of requests with Ashcon.
      .build();

  @NonNull
  private final SewerSystem<String, BanUser> userLoader = SewerSystem
      .<String, BanUser>builder(
          "parse identifier",
          ImmediatePipeHandler.of(identifier -> {
            if (identifier.length() < 3) {
              // Too short to be a username.
              throw new IllegalArgumentException("Username \"" + identifier + "\" is too short.");
            }

            if (identifier.length() > 16) {
              // Too long to be a username.
              if (identifier.length() != 32 && identifier.length() != 36) {
                throw new IllegalArgumentException("Username/UUID \"" + identifier + "\" has an invalid length.");
              }

              if (identifier.length() == 32) {
                identifier = StringUtils.rehyphenUuid(identifier);
              }

              final UUID uuid = UUID.fromString(identifier);
              if (BanUser.CONSOLE.getUuid().equals(uuid)) {
                return BanUser.CONSOLE;
              }

              if (uuid.version() != 4) {
                // Not online mode UUID.
                throw new IllegalArgumentException("UUID \"" + identifier + "\" is not an online-mode UUID");
              }

              // Let's check if they're already cached.
              final BanUser cachedUser = this.banUserCache.getIfPresent(uuid);
              if (cachedUser != null) {
                return cachedUser;
              }
            } else {
              // This is a name. Let's check if they're already cached.
              final UUID uuid = this.usernameUuidCache.getIfPresent(identifier);
              if (uuid != null) {
                final BanUser cachedUser = this.banUserCache.getIfPresent(uuid);
                if (cachedUser != null) {
                  return cachedUser;
                }
              }
            }
            final String finalIdentifier = identifier;

            // The user is not cached, so we need to fetch their data.
            final BanUser user = HttpUtils.get(API_BASE + "user/" + finalIdentifier)
                .map(json -> BanPlugin.COMPACT_GSON.fromJson(json, AshconUser.class).toBanUser(this))
                .orElseThrow(() -> new IllegalArgumentException(
                    "No user with the username/UUID \"" + finalIdentifier + "\" exists."));

            this.usernameUuidCache.put(user.getUsername(), user.getUuid());

            return user;
          })
      )
      .build();

  @NonNull
  private final SewerSystem<BanUser, UUID> uuidFromUser = SewerSystem
      .builder("get uuid", ImmediatePipeHandler.of(BanUser::getUuid)).build();

  @NonNull
  private final SewerSystem<BanUser, String> usernameFromUser = SewerSystem
      .builder("get username", ImmediatePipeHandler.of(BanUser::getUsername)).build();

  @NonNull
  private final SewerSystem<BanUser, UsernameHistory> usernameHistoryFromUser = SewerSystem
      .builder("get username history", ImmediatePipeHandler.of(BanUser::getUsernameHistory)).build();

  public MojangApiAshcon(
      @NonNull final Executor velocityExecutor
  ) {
    this.velocityExecutor = velocityExecutor;
  }

  @Override
  @NonNull
  public Loadable<BanUser> getUser(@NonNull final String identifier) {
    return Loadable.builder(this.userLoader, identifier).executor(this.velocityExecutor).build();
  }

  @Override
  @NonNull
  public Loadable<BanUser> getUser(@NonNull final UUID uuid) {
    // We don't care a whole lot about hyper efficiency.
    return Loadable.builder(this.userLoader, uuid.toString()).executor(this.velocityExecutor).build();
  }

  @Override
  @NonNull
  public Loadable<String> getUsername(@NonNull final UUID uuid) {
    return Loadable.builder(this.usernameFromUser, this.getUser(uuid)).executor(this.velocityExecutor).build();
  }

  @Override
  @NonNull
  public Loadable<UsernameHistory> getUsernameHistory(@NonNull final UUID uuid) {
    return Loadable.builder(this.usernameHistoryFromUser, this.getUser(uuid)).executor(this.velocityExecutor).build();
  }

  @Override
  @NonNull
  public Loadable<UUID> getUuid(@NonNull final String identifier) {
    return Loadable.builder(this.uuidFromUser, this.getUser(identifier)).executor(this.velocityExecutor).build();
  }

  static class AshconUser {
    @SerializedName("uuid")
    UUID uuid;

    @SerializedName("username")
    String username;

    @SerializedName("username_history")
    List<UsernameHistory.Entry> history;

    @NonNull
    BanUser toBanUser(@NonNull final IMojangApi mojangApi) {
      return new BanUser(
          this.uuid,
          this.username,
          new UsernameHistory(this.uuid, this.history == null ? Collections.emptyList() : this.history)
      );
    }
  }
}
