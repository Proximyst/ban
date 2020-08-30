package com.proximyst.ban.event.subscriber;

import com.google.inject.Inject;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.manager.PunishmentManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MutedPlayerChatSubscriber {
  @NonNull
  private final PunishmentManager manager;

  @NonNull
  private final MessagesConfig messagesConfig;

  @Inject
  public MutedPlayerChatSubscriber(@NonNull PunishmentManager manager, @NonNull MessagesConfig messagesConfig) {
    this.manager = manager;
    this.messagesConfig = messagesConfig;
  }

  @Subscribe
  public void onChat(PlayerChatEvent event) {
    manager.getActiveMute(event.getPlayer().getUniqueId())
        .join() // This *should* be fast, and only on one player's connection thread
        .ifPresent(mute -> {
          event.setResult(PlayerChatEvent.ChatResult.denied());
          event.getPlayer().sendMessage(
              MiniMessage.get()
                  .parse(
                      mute.getReason()
                          .map($ -> messagesConfig.getMuteMessageReason())
                          .orElse(messagesConfig.getMuteMessageReasonless())
                      // TODO: Placeholders
                  )
          );
        });
  }
}
