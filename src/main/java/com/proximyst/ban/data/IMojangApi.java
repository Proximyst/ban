package com.proximyst.ban.data;

import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface IMojangApi {
  /**
   * Get the UUID of a username.
   *
   * @param username The username to get the UUID of.
   * @return The UUID of the username.
   */
  @NonNull Optional<UUID> getUuidFromUsername(@NonNull String username);

  /**
   * Get the username of a UUID.
   *
   * @param uuid The UUID to get the username of.
   * @return The username of the UUID.
   */
  @NonNull Optional<String> getUsernameFromUuid(@NonNull UUID uuid);
}
