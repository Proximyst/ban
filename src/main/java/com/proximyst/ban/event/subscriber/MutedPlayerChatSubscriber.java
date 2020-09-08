package com.proximyst.ban.event.subscriber;

import com.google.inject.Inject;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.manager.PunishmentManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MutedPlayerChatSubscriber {
  @NonNull
  private final BanPlugin main;

  @NonNull
  private final PunishmentManager manager;

  @NonNull
  private final MessagesConfig messagesConfig;

  @Inject
  public MutedPlayerChatSubscriber(
      @NonNull BanPlugin main,
      @NonNull PunishmentManager manager,
      @NonNull MessagesConfig messagesConfig
  ) {
    this.main = main;
    this.manager = manager;
    this.messagesConfig = messagesConfig;
  }

  @Subscribe
  public void onChat(PlayerChatEvent event) {
    manager.getActiveMute(event.getPlayer().getUniqueId())
        .join() // This *should* be fast, and only on one player's connection thread
        .ifPresent(mute -> {
          event.setResult(ChatResult.denied());
          main.getMessageManager().formatMessageWith(
              mute.getReason().isPresent()
                  ? messagesConfig.muteMessageReason
                  : messagesConfig.muteMessageReasonless,
              mute
          ).thenAccept(event.getPlayer()::sendMessage);
        });
  }
}
