package com.proximyst.ban.model;

import com.proximyst.ban.data.IMojangApi;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BanUser {
  @NonNull
  private final UUID uuid;

  @NonNull
  private final LoadableData<String> username;

  @NonNull
  private final UsernameHistory usernameHistory;

  public BanUser(@NonNull UUID uuid, @NonNull IMojangApi mojangApi) {
    this.uuid = uuid;
    this.username = new LoadableData<String>(
        mojangApi
            .getUsernameFromUuid(uuid)
            .orElseThrow(() -> new IllegalArgumentException("unknown user \"" + uuid + "\""))
            .thenApply(opt -> opt.orElse(null))
    );
    this.usernameHistory = new UsernameHistory(uuid, mojangApi);
  }

  public BanUser(@NonNull UUID uuid, @NonNull String username, @NonNull IMojangApi mojangApi) {
    this(
        uuid,
        username,
        new UsernameHistory(uuid, mojangApi)
    );
  }

  public BanUser(@NonNull UUID uuid, @NonNull String username, @NonNull UsernameHistory usernameHistory) {
    this.uuid = uuid;
    this.username = new LoadableData<>(username);
    this.usernameHistory = usernameHistory;
  }

  @NonNull
  public UUID getUuid() {
    return uuid;
  }

  @NonNull
  public LoadableData<String> getUsername() {
    return username;
  }

  @NonNull
  public UsernameHistory getUsernameHistory() {
    return usernameHistory;
  }
}
