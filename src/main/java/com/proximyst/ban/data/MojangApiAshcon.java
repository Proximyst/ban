package com.proximyst.ban.data;

import com.google.gson.annotations.SerializedName;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.utils.HttpUtils;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MojangApiAshcon implements IMojangApi {
  private static final String API_BASE = "https://api.ashcon.app/mojang/v2/";

  @Override
  @NonNull
  public Optional<UUID> getUuidFromUsername(@NonNull String username) {
    return HttpUtils.get(API_BASE + "user/" + username)
        .map(json -> BanPlugin.COMPACT_GSON.fromJson(json, AshconUser.class).uuid);
  }

  @Override
  @NonNull
  public Optional<String> getUsernameFromUuid(@NonNull UUID uuid) {
    return HttpUtils.get(API_BASE + "user/" + uuid)
        .map(json -> BanPlugin.COMPACT_GSON.fromJson(json, AshconUser.class).username);
  }

  static class AshconUser {
    @SerializedName("uuid")
    UUID uuid;

    @SerializedName("username")
    String username;
  }
}
