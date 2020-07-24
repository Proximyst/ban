package com.proximyst.ban.data.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.annotations.SerializedName;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.data.IMojangApi;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.LoadableData;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.model.UsernameHistory.Entry;
import com.proximyst.ban.utils.HttpUtils;
import com.proximyst.ban.utils.StringUtils;
import com.proximyst.ban.utils.ThrowableUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MojangApiAshcon implements IMojangApi {
  private static final String API_BASE = "https://api.ashcon.app/mojang/v2/";

  private final Cache<UUID, BanUser> banUserCache = CacheBuilder.newBuilder()
      .maximumSize(512)
      .initialCapacity(512)
      .expireAfterWrite(1, TimeUnit.MINUTES) // We're not too scared of requests with Ashcon.
      .build();

  private final Cache<String, UUID> usernameUuidCache = CacheBuilder.newBuilder()
      .maximumSize(512)
      .initialCapacity(512)
      .expireAfterWrite(1, TimeUnit.MINUTES) // We're not too scared of requests with Ashcon.
      .build();

  @Override
  @NonNull
  public Optional<UUID> getUuidFromUsername(@NonNull String username) {
    try {
      return Optional.ofNullable(
          usernameUuidCache.get(
              username,
              () -> getUser(username)
                  .map(BanUser::getUuid)
                  .orElse(null)
          )
      );
    } catch (ExecutionException ex) {
      ThrowableUtils.sneakyThrow(ex.getCause() == null ? ex : ex.getCause());
      throw new RuntimeException();
    }
  }

  @Override
  @NonNull
  public Optional<CompletableFuture<Optional<String>>> getUsernameFromUuid(@NonNull UUID uuid) {
    return getUser(uuid)
        .map(BanUser::getUsername)
        .map(LoadableData::getAndLoad);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  @NonNull
  public Optional<CompletableFuture<Optional<List<Entry>>>> getUsernameHistory(@NonNull UUID uuid) {
    // Generics are taking a dump here, so we've gotta do a little trickery.
    return (Optional) getUser(uuid)
        .map(BanUser::getUsernameHistory)
        .map(UsernameHistory::getAndLoad);
  }

  @Override
  @NonNull
  public Optional<BanUser> getUser(@NonNull String identifier) {
    if (identifier.length() < 3) {
      // Too short to be a username.
      throw new IllegalArgumentException("username \"" + identifier + "\" is too short");
    }

    if (identifier.length() > 16) {
      // Too long to be a username.
      if (identifier.length() != 32 && identifier.length() != 36) {
        throw new IllegalArgumentException("username/uuid \"" + identifier + "\" has an invalid length");
      }

      if (identifier.length() == 32) {
        identifier = StringUtils.rehyphenUuid(identifier);
      }

      // It's a UUID; pass to the other #getUser to cache the UUID properly.
      return getUser(UUID.fromString(identifier));
    }

    UUID uuid = usernameUuidCache.getIfPresent(identifier);
    if (uuid != null) {
      return getUser(uuid);
    }

    Optional<BanUser> user = HttpUtils.get(API_BASE + "user/" + identifier)
        .map(json -> BanPlugin.COMPACT_GSON.fromJson(json, AshconUser.class).toBanUser(this));

    user.ifPresent(u -> usernameUuidCache.put(
        u.getUsername()
            .getIfPresent()
            .orElseThrow(() -> new IllegalStateException("loaded username cannot be null")),
        u.getUuid()
    ));

    return user;
  }

  @Override
  @NonNull
  public Optional<BanUser> getUser(@NonNull UUID uuid) {
    if (uuid.version() != 4) {
      // Not online mode UUID.
      throw new IllegalArgumentException("uuid \"" + uuid + "\" is not an online-mode uuid");
    }

    try {
      return Optional.ofNullable(
          banUserCache.get(
              uuid,
              () -> {
                BanUser user = HttpUtils.get(API_BASE + "user/" + uuid)
                    .map(json -> BanPlugin.COMPACT_GSON.fromJson(json, AshconUser.class).toBanUser(this))
                    .orElse(null);
                if (user != null) {
                  usernameUuidCache.put(
                      user.getUsername()
                          .getIfPresent()
                          .orElseThrow(() -> new IllegalStateException("loaded username cannot be null")),
                      user.getUuid()
                  );
                  return user;
                }

                return null;
              }
          )
      );
    } catch (ExecutionException ex) {
      ThrowableUtils.sneakyThrow(ex.getCause() == null ? ex : ex.getCause());
      throw new RuntimeException();
    }
  }

  static class AshconUser {
    @SerializedName("uuid")
    UUID uuid;

    @SerializedName("username")
    String username;

    @SerializedName("username_history")
    List<UsernameHistory.Entry> history;

    @NonNull
    BanUser toBanUser(@NonNull IMojangApi mojangApi) {
      return new BanUser(
          uuid,
          username,
          new UsernameHistory(uuid, history == null ? Collections.emptyList() : history)
      );
    }
  }
}
