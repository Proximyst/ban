package com.proximyst.ban.data;

import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
  @NonNull Optional<CompletableFuture<Optional<String>>> getUsernameFromUuid(@NonNull UUID uuid);

  /**
   * Get the username history of a UUID.
   *
   * @param uuid The UUID to get the username history of.
   * @return The username history of the UUID, unsorted.
   */
  @NonNull Optional<CompletableFuture<Optional<List<UsernameHistory.Entry>>>> getUsernameHistory(@NonNull UUID uuid);

  /**
   * Get a populated {@link BanUser} for the user given.
   *
   * @param identifier Either the UUID of the user in string form (with or without hyphens), or their username.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull Optional<BanUser> getUser(@NonNull String identifier);

  /**
   * Get a populated {@link BanUser} from the user given.
   *
   * @param uuid The UUID of the user.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull Optional<BanUser> getUser(@NonNull UUID uuid);

  /**
   * Get a populated {@link BanUser} for the user given.
   *
   * @param identifier Either the UUID of the user in string form (with or without hyphens), or their username.
   * @return Data about the user, fully populated with known data.
   */
  default @NonNull CompletableFuture<Optional<BanUser>> getUserFuture(
      @NonNull String identifier,
      @NonNull BanPlugin main
  ) {
    final CompletableFuture<Optional<BanUser>> future = new CompletableFuture<>();
    main.getProxyServer().getScheduler().buildTask(main, () -> future.complete(getUser(identifier)))
        .schedule();
    return future;
  }

  /**
   * Get a populated {@link BanUser} from the user given.
   *
   * @param uuid The UUID of the user.
   * @return Data about the user, fully populated with known data.
   */
  default @NonNull CompletableFuture<Optional<BanUser>> getUserFuture(@NonNull UUID uuid, @NonNull BanPlugin main) {
    final CompletableFuture<Optional<BanUser>> future = new CompletableFuture<>();
    main.getProxyServer().getScheduler().buildTask(main, () -> future.complete(getUser(uuid)))
        .schedule();
    return future;
  }
}
