package com.proximyst.ban.event.subscriber;

import com.google.inject.Inject;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.manager.PunishmentManager;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BannedPlayerJoinSubscriber {
  @NonNull
  private final PunishmentManager manager;

  @NonNull
  private final MessagesConfig messagesConfig;

  @Inject
  public BannedPlayerJoinSubscriber(@NonNull PunishmentManager manager, @NonNull MessagesConfig messagesConfig) {
    this.manager = manager;
    this.messagesConfig = messagesConfig;
  }

  @Subscribe
  public void onJoinServer(LoginEvent event) {
    manager.getActiveBan(event.getPlayer().getUniqueId())
        .join() // This *should* be fast, and only on one player's connection thread
        .ifPresent(ban -> {
          event.setResult(ComponentResult.denied(
              MiniMessage.get()
                  .parse(
                      ban.getReason()
                          .map($ -> messagesConfig.getBanMessageReason())
                          .orElse(messagesConfig.getBanMessageReasonless())
                  )
          ));
        });
  }
}
