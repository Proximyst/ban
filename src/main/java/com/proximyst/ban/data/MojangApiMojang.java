package com.proximyst.ban.data;

import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MojangApiMojang implements IMojangApi {
  @Override
  @NonNull
  public Optional<UUID> getUuidFromUsername(@NonNull String username) {
    // TODO(Proximyst)
    return Optional.empty();
  }

  @Override
  @NonNull
  public Optional<String> getUsernameFromUuid(@NonNull UUID uuid) {
    // TODO(Proximyst)
    return Optional.empty();
  }
}
