package com.proximyst.ban.model;

import java.util.Collections;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BanUser {
  /**
   * The UUID used for the console in data storage.
   */
  @NonNull
  private static UUID CONSOLE_UUID = new UUID(0, 0);

  @NonNull
  public static final BanUser CONSOLE = new BanUser(
      CONSOLE_UUID,
      "CONSOLE",
      new UsernameHistory(CONSOLE_UUID, Collections.emptyList())
  );

  @NonNull
  private final UUID uuid;

  @NonNull
  private final String username;

  @NonNull
  private final UsernameHistory usernameHistory;

  public BanUser(@NonNull UUID uuid, @NonNull String username, @NonNull UsernameHistory usernameHistory) {
    this.uuid = uuid;
    this.username = username;
    this.usernameHistory = usernameHistory;
  }

  @NonNull
  public UUID getUuid() {
    return uuid;
  }

  @NonNull
  public String getUsername() {
    return username;
  }

  @NonNull
  public UsernameHistory getUsernameHistory() {
    return usernameHistory;
  }
}
