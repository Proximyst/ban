package com.proximyst.ban.event.subscriber;

import com.google.inject.Inject;
import com.proximyst.ban.manager.UserManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CacheUpdatePlayerJoinSubscriber {
  @NonNull
  private final UserManager userManager;

  @Inject
  public CacheUpdatePlayerJoinSubscriber(
      @NonNull UserManager userManager
  ) {
    this.userManager = userManager;
  }

  @Subscribe
  public void onJoinServer(LoginEvent event) {
    userManager.updateUser(event.getPlayer().getUniqueId());
  }
}
