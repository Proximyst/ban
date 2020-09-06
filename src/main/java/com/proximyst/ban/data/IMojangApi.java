package com.proximyst.ban.data;

import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.sewer.loadable.Loadable;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface IMojangApi {
  /**
   * Get the UUID of an identifier.
   *
   * @param identifier The identifier to get the UUID of.
   * @return The UUID of the identifier.
   */
  @NonNull Loadable<UUID> getUuid(@NonNull String identifier);

  /**
   * Get the username of a UUID.
   *
   * @param uuid The UUID to get the username of.
   * @return The username of the UUID.
   */
  @NonNull Loadable<String> getUsername(@NonNull UUID uuid);

  /**
   * Get the username history of a UUID.
   *
   * @param uuid The UUID to get the username history of.
   * @return The username history of the UUID, unsorted.
   */
  @NonNull Loadable<UsernameHistory> getUsernameHistory(@NonNull UUID uuid);

  /**
   * Get a populated {@link BanUser} for the user given.
   *
   * @param identifier Either the UUID of the user in string form (with or without hyphens), or their username.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull Loadable<BanUser> getUser(@NonNull String identifier);

  /**
   * Get a populated {@link BanUser} from the user given.
   *
   * @param uuid The UUID of the user.
   * @return Data about the user, fully populated with known data.
   */
  @NonNull Loadable<BanUser> getUser(@NonNull UUID uuid);
}
