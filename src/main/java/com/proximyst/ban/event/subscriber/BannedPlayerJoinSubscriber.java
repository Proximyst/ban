package com.proximyst.ban.event.subscriber;

import com.google.inject.Inject;
import com.proximyst.ban.manager.PunishmentManager;
import com.proximyst.ban.model.Punishment;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BannedPlayerJoinSubscriber {
  private final PunishmentManager manager;

  @Inject
  public BannedPlayerJoinSubscriber(@NonNull PunishmentManager manager) {
    this.manager = manager;
  }

  @Subscribe
  public void onJoinServer(LoginEvent event) {
    if (manager.getActiveBan(event.getPlayer().getUniqueId()).filter(Punishment::currentlyApplies).isPresent()) {
      event.setResult(
          ComponentResult.denied(MiniMessage.get().parse("<rainbow>You are banned!"))
      );
    }
  }
}
